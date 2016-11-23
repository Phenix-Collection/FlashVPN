package com.polestar.multiaccount.model;

import java.io.Serializable;

/**
 * Created by hxx on 8/31/16.
 */
public class Feedback implements Serializable {
    public FeedbackContent feedback;
    public Feedback() {}


    public static class FeedbackContent implements Serializable {
        public String email;
        public String content;
        public String score;

        public FeedbackContent() {}
    }
}
