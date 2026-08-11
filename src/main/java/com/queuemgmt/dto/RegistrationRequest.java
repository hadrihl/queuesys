package com.queuemgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {
    private String userName;
    private String email;

    /**
     * The pincode emailed to the user via EmailVerificationService.generateAndSendCode.
     */
    private String pincode;
}
