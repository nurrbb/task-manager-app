package com.nurbb.taskmanagerapp.model.dto.response;

public record AuthResponse(String token) {
}
//User giriş yaptıktan sonra sunucunun geri döneceği yanıt.
//JWT (Json web token) erişim tokeni içeriyor)