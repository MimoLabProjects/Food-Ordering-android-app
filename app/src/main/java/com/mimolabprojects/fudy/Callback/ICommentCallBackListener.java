package com.mimolabprojects.fudy.Callback;

import com.mimolabprojects.fudy.Model.CommentModel;

import java.util.List;

public interface ICommentCallBackListener  {

    void onCommentLoadSuccess (List<CommentModel> commentModels);
    void onCommentLoadFailed(String message);
}
