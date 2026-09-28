package com.sabareeshrao.intellijlesson.ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextArea;
import com.sabareeshrao.intellijlesson.model.LessonCourse;
import com.sabareeshrao.intellijlesson.runtime.LessonRuntimeService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

public final class LessonToolWindowPanel extends JPanel {
    private final LessonRuntimeService runtime;

    private final JBLabel courseLabel = new JBLabel();
    private final JBLabel chapterLabel = new JBLabel();
    private final JBLabel lessonTitle = new JBLabel();
    private final JBLabel progressLabel = new JBLabel();
    private final JBLabel statusLabel = new JBLabel();

    private final JBTextArea questionArea = textArea();
    private final JBTextArea explanationArea = textArea();
    private final JBTextArea answerArea = textArea();

    private final JBComboBox<LessonItem> lessonSelector = new JBComboBox<>();
    private boolean rendering;

    public LessonToolWindowPanel(Project project) {
        super(new BorderLayout(8, 8));
        this.runtime = project.getService(LessonRuntimeService.class);

        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        courseLabel.setFont(courseLabel.getFont().deriveFont(Font.BOLD, courseLabel.getFont().getSize2D() + 1f));
        lessonTitle.setFont(lessonTitle.getFont().deriveFont(Font.BOLD, lessonTitle.getFont().getSize2D() + 2f));
        statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        top.add(courseLabel);
        top.add(Box.createVerticalStrut(4));
        top.add(lessonSelector);
        top.add(Box.createVerticalStrut(6));
        top.add(chapterLabel);
        top.add(Box.createVerticalStrut(6));
        top.add(lessonTitle);
        top.add(Box.createVerticalStrut(4));
        top.add(progressLabel);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(questionArea);
        text.add(Box.createVerticalStrut(12));
        text.add(explanationArea);
        text.add(Box.createVerticalStrut(12));
        text.add(answerArea);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        JButton previous = new JButton("◀ Previous");
        JButton replay = new JButton("Replay");
        JButton next = new JButton("Next ▶");
        buttons.add(previous);
        buttons.add(replay);
        buttons.add(next);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(statusLabel);
        bottom.add(Box.createVerticalStrut(8));
        bottom.add(buttons);

        add(top, BorderLayout.NORTH);
        add(ScrollPaneFactory.createScrollPane(text, true), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        previous.addActionListener(event -> applyResult(runtime.previous()));
        replay.addActionListener(event -> applyResult(runtime.replay()));
        next.addActionListener(event -> applyResult(runtime.next()));
        lessonSelector.addActionListener(event -> {
            if (rendering) {
                return;
            }
            LessonItem item = (LessonItem) lessonSelector.getSelectedItem();
            if (item != null) {
                applyResult(runtime.jumpToLesson(item.lessonNumber()));
            }
        });

        rebuildLessonSelector();
        render("Ready. Next executes the next lesson action in real IntelliJ.");
    }

    private void rebuildLessonSelector() {
        rendering = true;
        try {
            DefaultComboBoxModel<LessonItem> model = new DefaultComboBoxModel<>();
            for (LessonCourse.Lesson lesson : runtime.lessons()) {
                model.addElement(new LessonItem(lesson.lessonNumber(), lesson.title()));
            }
            lessonSelector.setModel(model);
        } finally {
            rendering = false;
        }
    }

    private void applyResult(LessonRuntimeService.Result result) {
        render(result.message());
    }

    private void render(String message) {
        rendering = true;
        try {
            courseLabel.setText(runtime.courseTitle());
            statusLabel.setText(message == null ? "" : message);

            runtime.current().ifPresentOrElse(current -> {
                LessonCourse.Lesson lesson = current.lesson();
                LessonCourse.Step step = current.step();

                chapterLabel.setText(
                        (current.bookTitle().isBlank() ? "" : current.bookTitle() + "  •  ") +
                                lesson.chapterTitle()
                );
                lessonTitle.setText(lesson.title());
                progressLabel.setText(
                        "Lesson " + lesson.lessonNumber() +
                                "  •  Step " + (current.stepIndex() + 1) +
                                " / " + lesson.steps().size() +
                                "  •  Action: " + step.action().name()
                );

                questionArea.setText(step.question());
                explanationArea.setText(step.explanation());
                answerArea.setVisible(current.finalStep());
                answerArea.setText(current.finalStep() ? lesson.answer() : "");

                LessonItem selected = new LessonItem(lesson.lessonNumber(), lesson.title());
                for (int i = 0; i < lessonSelector.getItemCount(); i++) {
                    if (Objects.equals(lessonSelector.getItemAt(i), selected)) {
                        lessonSelector.setSelectedIndex(i);
                        break;
                    }
                }
            }, () -> {
                chapterLabel.setText("");
                lessonTitle.setText("Course unavailable");
                progressLabel.setText("");
                questionArea.setText(runtime.loadError());
                explanationArea.setText("");
                answerArea.setVisible(false);
                if (message == null || message.isBlank()) {
                    statusLabel.setText(runtime.loadError());
                }
            });
        } finally {
            rendering = false;
        }

        revalidate();
        repaint();
    }

    private static JBTextArea textArea() {
        JBTextArea area = new JBTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setBorder(BorderFactory.createEmptyBorder());
        return area;
    }

    private record LessonItem(int lessonNumber, String title) {
        @Override
        public String toString() {
            return lessonNumber + " · " + title;
        }
    }
}
