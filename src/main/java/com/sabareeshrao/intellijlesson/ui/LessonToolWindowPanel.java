package com.sabareeshrao.intellijlesson.ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextArea;
import com.sabareeshrao.intellijlesson.model.LessonCourse;
import com.sabareeshrao.intellijlesson.runtime.LessonRuntimeService;
import com.sabareeshrao.intellijlesson.state.LessonProgressService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

public final class LessonToolWindowPanel extends JPanel {
    private static final int MIN_TEXT_SCALE = 80;
    private static final int MAX_TEXT_SCALE = 180;
    private static final int TEXT_SCALE_STEP = 10;

    private final LessonRuntimeService runtime;
    private final LessonProgressService progress;

    private final JBLabel courseLabel = new JBLabel();
    private final JBLabel chapterLabel = new JBLabel();
    private final JBLabel lessonTitle = new JBLabel();
    private final JBLabel progressLabel = new JBLabel();
    private final JBLabel statusLabel = new JBLabel();
    private final JBLabel scaleLabel = new JBLabel();

    private final WrappingTextArea questionArea = textArea();
    private final WrappingTextArea explanationArea = textArea();
    private final WrappingTextArea answerArea = textArea();

    private final JComboBox<LessonItem> lessonSelector = new JComboBox<>();
    private final float baseBodyFontSize;
    private final float baseLabelFontSize;
    private boolean rendering;

    public LessonToolWindowPanel(Project project) {
        super(new BorderLayout(8, 8));
        this.runtime = project.getService(LessonRuntimeService.class);
        this.progress = project.getService(LessonProgressService.class);
        this.baseBodyFontSize = questionArea.getFont().getSize2D();
        this.baseLabelFontSize = lessonTitle.getFont().getSize2D();

        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JPanel headingRow = new JPanel(new BorderLayout(8, 0));
        headingRow.add(courseLabel, BorderLayout.CENTER);
        headingRow.add(createTextSizeControls(), BorderLayout.EAST);

        statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        top.add(headingRow);
        top.add(Box.createVerticalStrut(5));
        top.add(lessonSelector);
        top.add(Box.createVerticalStrut(7));
        top.add(chapterLabel);
        top.add(Box.createVerticalStrut(7));
        top.add(lessonTitle);
        top.add(Box.createVerticalStrut(4));
        top.add(progressLabel);

        JPanel text = new JPanel(new GridBagLayout());
        text.setBorder(new EmptyBorder(8, 0, 8, 0));
        addTextRow(text, questionArea, 0);
        addTextRow(text, explanationArea, 1);
        addTextRow(text, answerArea, 2);

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = 3;
        filler.weightx = 1.0;
        filler.weighty = 1.0;
        filler.fill = GridBagConstraints.BOTH;
        text.add(Box.createGlue(), filler);

        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(text, true);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(0);

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
        add(scrollPane, BorderLayout.CENTER);
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
        applyTextScale(progress.textScalePercent());
        render("Ready. Next executes the next lesson action in real IntelliJ.");
    }

    private JPanel createTextSizeControls() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        JButton smaller = compactButton("A−", "Decrease lesson text size");
        JButton reset = compactButton("100%", "Reset lesson text size");
        JButton larger = compactButton("A+", "Increase lesson text size");

        smaller.addActionListener(event -> changeTextScale(-TEXT_SCALE_STEP));
        reset.addActionListener(event -> setTextScale(100));
        larger.addActionListener(event -> changeTextScale(TEXT_SCALE_STEP));

        controls.add(smaller);
        controls.add(scaleLabel);
        controls.add(reset);
        controls.add(larger);
        return controls;
    }

    private static JButton compactButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setMargin(new Insets(2, 6, 2, 6));
        button.setFocusable(false);
        return button;
    }

    private static void addTextRow(JPanel panel, JComponent component, int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(row == 0 ? 0 : 12, 0, 0, 0);
        panel.add(component, constraints);
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

    private void changeTextScale(int delta) {
        setTextScale(progress.textScalePercent() + delta);
    }

    private void setTextScale(int percent) {
        int clamped = Math.max(MIN_TEXT_SCALE, Math.min(MAX_TEXT_SCALE, percent));
        progress.setTextScalePercent(clamped);
        applyTextScale(clamped);
        revalidate();
        repaint();
    }

    private void applyTextScale(int percent) {
        float scale = percent / 100.0f;
        Font body = questionArea.getFont().deriveFont(Math.max(10f, baseBodyFontSize * scale));
        questionArea.setFont(body);
        explanationArea.setFont(body);
        answerArea.setFont(body);

        courseLabel.setFont(courseLabel.getFont().deriveFont(Font.BOLD, Math.max(11f, baseLabelFontSize * scale)));
        chapterLabel.setFont(chapterLabel.getFont().deriveFont(Math.max(10f, (baseLabelFontSize - 1f) * scale)));
        lessonTitle.setFont(lessonTitle.getFont().deriveFont(Font.BOLD, Math.max(12f, (baseLabelFontSize + 2f) * scale)));
        progressLabel.setFont(progressLabel.getFont().deriveFont(Math.max(10f, (baseLabelFontSize - 1f) * scale)));
        scaleLabel.setText(percent + "%");
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

                questionArea.setCaretPosition(0);
                explanationArea.setCaretPosition(0);
                answerArea.setCaretPosition(0);

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

    private static WrappingTextArea textArea() {
        WrappingTextArea area = new WrappingTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setBorder(BorderFactory.createEmptyBorder());
        area.setFocusable(false);
        return area;
    }

    private static final class WrappingTextArea extends JBTextArea {
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension preferred = getPreferredSize();
            return new Dimension(0, preferred.height);
        }
    }

    private record LessonItem(int lessonNumber, String title) {
        @Override
        public String toString() {
            return lessonNumber + " · " + title;
        }
    }
}
