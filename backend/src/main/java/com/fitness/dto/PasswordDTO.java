package com.fitness.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 修改密码DTO
 */
@Data
public class PasswordDTO {
    
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;
    
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20位")
    private String newPassword;
}
