package com.chuck.blog.repository;

import com.chuck.blog.entity.Article;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ArticleRepository extends MongoRepository<Article, Long> {
    Article findByGuid(String guid);
}