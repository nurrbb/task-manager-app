package com.nurbb.taskmanagerapp.model.dto.request;

public record AuthRequest(
        String username,
        String password
)
{ }
//Kullanıcının sisteme giriş yaparken gönderdiği form verilerini taşır