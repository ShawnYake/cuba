/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.cuba.desktop.gui.components;

import com.haulmont.cuba.desktop.sys.validation.ValidationAwareAction;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.Component;
import org.apache.commons.lang.ObjectUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

import static com.haulmont.cuba.gui.ComponentsHelper.findActionById;

/**
 * Encapsulates {@link com.haulmont.cuba.gui.components.Component.ActionsHolder} functionality for desktop frames and
 * windows.
 *
 * @author krivopustov
 */
public class DesktopFrameActionsHolder {

    private List<Action> actionList = new LinkedList<>();
    private Map<Action, KeyStroke> shortcutActions = new HashMap<>();

    private Component component;
    private JPanel panel;

    public DesktopFrameActionsHolder(Component component, JPanel panel) {
        this.component = component;
        this.panel = panel;
    }

    public void addAction(Action action) {
        int index = findActionById(actionList, action.getId());
        if (index < 0) {
            index = actionList.size();
        }

        addAction(action, index);
    }

    public void addAction(Action action, int index) {
        int oldIndex = findActionById(actionList, action.getId());
        if (oldIndex >= 0) {
            removeAction(actionList.get(oldIndex));
            if (index > oldIndex) {
                index--;
            }
        }

        if (action.getShortcut() != null) {
            KeyStroke keyStroke = DesktopComponentsHelper.convertKeyCombination(action.getShortcut());
            InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            inputMap.put(keyStroke, action.getId());
            ActionMap actionMap = panel.getActionMap();
            actionMap.put(action.getId(), new ValidationAwareAction() {
                @Override
                public void actionPerformedAfterValidation(ActionEvent e) {
                    action.actionPerform(component);
                }
            });
            shortcutActions.put(action, keyStroke);
        }

        actionList.add(index, action);
    }

    public void removeAction(Action action) {
        if (actionList.remove(action)) {
            if (action.getShortcut() != null) {
                InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
                ActionMap actionMap = panel.getActionMap();
                KeyStroke keyStroke = shortcutActions.get(action);
                if (keyStroke != null) {
                    inputMap.remove(keyStroke);
                    actionMap.remove(action.getId());
                }
            }
        }
    }

    public void removeAction(String id) {
        Action action = getAction(id);
        if (action != null) {
            removeAction(action);
        }
    }

    public void removeAllActions() {
        for (Action action : new ArrayList<>(actionList)) {
            removeAction(action);
        }
    }

    public Collection<Action> getActions() {
        return Collections.unmodifiableCollection(actionList);
    }

    public Action getAction(String id) {
        for (com.haulmont.cuba.gui.components.Action action : getActions()) {
            if (ObjectUtils.equals(action.getId(), id)) {
                return action;
            }
        }
        return null;
    }
}