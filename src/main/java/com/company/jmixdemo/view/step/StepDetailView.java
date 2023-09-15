package com.company.jmixdemo.view.step;

import com.company.jmixdemo.entity.Step;

import com.company.jmixdemo.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "steps/:id", layout = MainView.class)
@ViewController("Step.detail")
@ViewDescriptor("step-detail-view.xml")
@EditedEntityContainer("stepDc")
public class StepDetailView extends StandardDetailView<Step> {
}