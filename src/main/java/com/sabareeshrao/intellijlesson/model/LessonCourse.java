package com.sabareeshrao.intellijlesson.model;

import com.google.gson.JsonObject;

import java.util.List;

public record LessonCourse(
        String title,
        List<BookRef> books,
        List<Chapter> chapters,
        List<Lesson> lessons
) {
    public record BookRef(String id, String title, String subtitle, int chapterStart, int chapterEnd) {}

    public record Chapter(
            String id,
            String title,
            String subtitle,
            int ordinal,
            List<Integer> lessonNumbers
    ) {}

    public record Lesson(
            int lessonNumber,
            int questionId,
            String chapterId,
            String chapterTitle,
            int chapterOrdinal,
            String title,
            String answer,
            String infoLanguage,
            String projectImpact,
            List<Step> steps
    ) {}

    public record Step(
            String title,
            String question,
            String explanation,
            String requiredCapability,
            Action action
    ) {}

    public record Action(String name, JsonObject data) {}

    public int lessonIndex(int lessonNumber) {
        for (int i = 0; i < lessons.size(); i++) {
            if (lessons.get(i).lessonNumber() == lessonNumber) {
                return i;
            }
        }
        return -1;
    }

    public String bookTitleForChapter(int chapterOrdinal) {
        return books.stream()
                .filter(book -> chapterOrdinal >= book.chapterStart() && chapterOrdinal <= book.chapterEnd())
                .map(BookRef::title)
                .findFirst()
                .orElse("");
    }
}
