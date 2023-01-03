package com.sparta.tigercave.controller;

import com.sparta.tigercave.dto.UsersDto;
import com.sparta.tigercave.entity.Users;
import com.sparta.tigercave.entity.UsersRoleEnum;
import com.sparta.tigercave.exception.CustomException;
import com.sparta.tigercave.jwt.JwtUtil;
import com.sparta.tigercave.repository.UsersRepository;
import com.sparta.tigercave.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static com.sparta.tigercave.exception.ErrorCode.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UsersController {

    private final UsersService usersService;
    private final UsersRepository usersRepository;

    private final JwtUtil jwtUtil;

    private static final String ADMIN_TOKEN = "AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC";

    @PostMapping("/signup")
    public ResponseEntity signUp(@RequestBody @Validated UsersDto.signUpRequestDto signUpRequestDto, BindingResult bindingresult){

        //유효성 검사 실패할 경우 에러메세지 반환
        if(bindingresult.hasErrors()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingresult.getAllErrors().toString());
        }

        //이미 존재한 user인지 확인
        Optional<Users> check_result = usersRepository.findByUsername(signUpRequestDto.getUsername());
        check_result.ifPresent(m -> {
            throw new CustomException(DUPLICATE_USERNAME);
        });

        UsersRoleEnum role = UsersRoleEnum.USER;

        if(signUpRequestDto.isAdmin()){
            if(!signUpRequestDto.getAdminToken().equals(ADMIN_TOKEN)){
                throw new CustomException(ADMIN_PASSWORD_NOT_FOUND);
            }
            role = UsersRoleEnum.ADMIN;
        }

        usersService.signUp(signUpRequestDto, role);
        return new ResponseEntity<>("회원가입에 성공하였습니다", HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestParam String username, @RequestParam String password, HttpServletResponse response){

        UsersDto.loginResponseDto users = usersService.login(username, password);
        
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(users.getUsername(), users.getRole()));
        return new ResponseEntity("로그인에 성공하였습니다.", HttpStatus.OK);
    }
}
