package com.sabareeshrao.intellijlesson.course;

import com.sabareeshrao.intellijlesson.model.LessonCourse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LessonCourseLoaderTest {
    @TempDir
    Path tempDir;

    @Test
    void loadsCourseAndReferencedLessonFiles() throws Exception {
        Path lessonDir = Files.createDirectories(tempDir.resolve("simulation/lessons"));

        Files.writeString(tempDir.resolve("simulation/course.json"), """
                {
                  "title": "Demo Course",
                  "books": [{
                    "id": "book-1",
                    "title": "Book 1",
                    "subtitle": "",
                    "chapterStart": 1,
                    "chapterEnd": 1
                  }],
                  "chapters": [{
                    "id": "chapter-001",
                    "title": "1: Start",
                    "subtitle": "Demo",
                    "lessons": ["lessons/0001.json"]
                  }]
                }
                """);

        Files.writeString(lessonDir.resolve("0001.json"), """
                {
                  "lesson_number": 1,
                  "question_id": 1,
                  "title": "Open a file",
                  "answer": "Answer",
                  "info_language": "te-Latn",
                  "project_impact": "none",
                  "steps": [{
                    "title": "Open",
                    "question": "Question",
                    "why_te": "Telugu explanation",
                    "required_capability": "open-file",
                    "action": {
                      "action": "openFile",
                      "data": {"file": "pom.xml"}
                    }
                  }]
                }
                """);

        LessonCourse course = new LessonCourseLoader().load(tempDir);

        assertEquals("Demo Course", course.title());
        assertEquals(1, course.books().size());
        assertEquals(1, course.chapters().size());
        assertEquals(1, course.lessons().size());
        assertEquals("openFile", course.lessons().getFirst().steps().getFirst().action().name());
        assertEquals(
                "pom.xml",
                course.lessons().getFirst().steps().getFirst().action().data().get("file").getAsString()
        );
    }
}
