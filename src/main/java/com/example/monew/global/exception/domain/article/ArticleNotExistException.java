package com.example.monew.global.exception.domain.article;

import com.example.monew.global.exception.CustomException;
import com.example.monew.global.exception.ErrorCode;

import java.util.HashMap;
import java.util.UUID;

public class ArticleNotExistException extends CustomException {
    public ArticleNotExistException(UUID articleId) {
        super(ErrorCode.ARTICLE_NOT_EXIST, new HashMap<>(){
            {put("articleId", articleId);}
        });
    }
}
