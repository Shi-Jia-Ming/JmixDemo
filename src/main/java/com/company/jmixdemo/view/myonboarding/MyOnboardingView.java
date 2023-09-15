package com.company.jmixdemo.view.myonboarding;


import com.company.jmixdemo.entity.User;
import com.company.jmixdemo.entity.UserStep;
import com.company.jmixdemo.view.main.MainView;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@Route(value = "MyOnboardingView", layout = MainView.class)
@ViewController("MyOnboardingView")
@ViewDescriptor("my-onboarding-view.xml")
public class MyOnboardingView extends StandardView {
    @ViewComponent
    private CollectionLoader<UserStep> userStepsDl;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private UiComponents uiComponents;

    @ViewComponent
    private DataGrid<UserStep> userStepsDataGrid;

    @ViewComponent
    private Span completedStepsLabel;

    @ViewComponent
    private Span overdueStepsLabel;

    @ViewComponent
    private Span totalStepsLabel;

    @ViewComponent
    private CollectionContainer<UserStep> userStepsDc;

    @ViewComponent
    private DataContext dataContext;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        final User user = (User) currentAuthentication.getUser();
        userStepsDl.setParameter("user", user);
        userStepsDl.load();

        updateLabels();
    }

    @Subscribe
    public void onInit(final InitEvent event) {
        Grid.Column<UserStep> completedColumn = userStepsDataGrid.addComponentColumn(userStep -> {
            Checkbox checkbox = uiComponents.create(Checkbox.class);
            checkbox.setValue(userStep.getCompletedDate() != null);
            checkbox.addValueChangeListener(e -> {
                if (userStep.getCompletedDate() == null) {
                    userStep.setCompletedDate(LocalDate.now());
                } else {
                    userStep.setCompletedDate(null);
                }
            });
            return checkbox;
        });
        completedColumn.setFlexGrow(0);
        completedColumn.setWidth("4em");
        userStepsDataGrid.setColumnPosition(completedColumn, 0);

        Grid.Column<UserStep> dueDate = userStepsDataGrid.getColumnByKey("dueDate");
        if (dueDate != null) {
            dueDate.setPartNameGenerator(userStep ->
                    isOverdue(userStep) ? "overdue-step" : null);
        }
    }

    private void updateLabels() {
        totalStepsLabel.setText("Total steps: " + userStepsDc.getItems().size());

        long completedCount = userStepsDc.getItems().stream()
                .filter(us -> us.getCompletedDate() != null)
                .count();
        completedStepsLabel.setText("Completed steps: " + completedCount);

        long overdueCount = userStepsDc.getItems().stream()
                .filter(this::isOverdue)
                .count();
        overdueStepsLabel.setText("Overdue steps: " + overdueCount);
    }

    private boolean isOverdue(UserStep us) {
        return us.getCompletedDate() == null
                && us.getDueDate() != null
                && us.getDueDate().isBefore(LocalDate.now());
    }

    @Subscribe("saveButton")
    public void onSaveButtonClick(final ClickEvent<Button> event) {
        dataContext.save();
        close(StandardOutcome.SAVE);
    }

    @Subscribe("discardButton")
    public void onDiscardButtonClick(final ClickEvent<Button> event) {
        close(StandardOutcome.DISCARD);
    }
}