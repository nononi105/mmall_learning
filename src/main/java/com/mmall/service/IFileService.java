package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by NoNo_Ni on 2020/4/8.
 */
public interface IFileService {
    String upload(MultipartFile file, String path);
}
