package com.chuck.blog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Article {
    @Id
    private String id;
    private String guid;
    private String title;
    private String summary;
    private String content;
    private Date date;
}
