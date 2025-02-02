/*
 * Copyright 2012 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jbpm.form.builder.ng.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.guvnor.common.services.shared.config.AppConfigService;
import org.guvnor.common.services.shared.config.ApplicationPreferences;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jbpm.form.builder.ng.client.resources.ShowcaseResources;
import org.uberfire.client.UberFirePreferences;
import org.uberfire.client.mvp.AbstractWorkbenchPerspectiveActivity;
import org.uberfire.client.mvp.ActivityManager;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.widgets.menu.WorkbenchMenuBarPresenter;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 *
 */
@EntryPoint
public class ShowcaseEntryPoint {

    private String[] menuItems = new String[] { "Message List", "Multiples Perspectives" };
    @Inject
    private Caller<AppConfigService> appConfigService;
    @Inject
    private PlaceManager placeManager;
    @Inject
    private WorkbenchMenuBarPresenter menubar;
    @Inject
    private ActivityManager activityManager;
    @Inject
    private SyncBeanManager iocManager;

    @AfterInitialization
    public void startApp() {
        loadPreferences();
        setupMenu();
        hideLoadingPopup();
    }

    private void loadPreferences() {
        // Ensure CSS has been loaded
        ShowcaseResources.INSTANCE.showcaseCss().ensureInjected();
        UberFirePreferences.setProperty("org.uberfire.client.workbench.widgets.listbar.context.disable", true);
        appConfigService.call(new RemoteCallback<Map<String, String>>() {
            @Override
            public void callback(final Map<String, String> response) {
                ApplicationPreferences.setUp(response);
            }
        }).loadPreferences();

    }

    private void setupMenu() {
        final AbstractWorkbenchPerspectiveActivity defaultPerspective = getDefaultPerspectiveActivity();
        final Menus menus = MenuFactory.newTopLevelMenu("Home").respondsWith(new Command() {
            @Override
            public void execute() {
                if (defaultPerspective != null) {
                    placeManager.goTo(new DefaultPlaceRequest(defaultPerspective.getIdentifier()));
                } else {
                    Window.alert("Default perspective not found.");
                }
            }
        }).endMenu()
        .newTopLevelMenu("Uberfire Demo").withItems(getMenuItems()).endMenu()
        .newTopLevelMenu("Logout").respondsWith(new Command() {
                    @Override
                    public void execute() {
                        redirect("/uf_logout");
                    }
                }).endMenu().build();
        menubar.addMenus(menus);
    }

    private List<? extends MenuItem> getMenuItems() {
        Arrays.sort(menuItems);
        final List<MenuItem> result = new ArrayList<MenuItem>(menuItems.length);
        for (final String menuItem : menuItems) {
            result.add(MenuFactory.newSimpleItem(menuItem).respondsWith(new Command() {
                @Override
                public void execute() {
                    placeManager.goTo(new DefaultPlaceRequest(menuItem));
                }
            }).endMenu().build().getItems().iterator().next());
        }
        return result;
    }

    private AbstractWorkbenchPerspectiveActivity getDefaultPerspectiveActivity() {
        AbstractWorkbenchPerspectiveActivity defaultPerspective = null;
        final Collection<IOCBeanDef<AbstractWorkbenchPerspectiveActivity>> perspectives = iocManager
                .lookupBeans(AbstractWorkbenchPerspectiveActivity.class);
        final Iterator<IOCBeanDef<AbstractWorkbenchPerspectiveActivity>> perspectivesIterator = perspectives.iterator();
        outer_loop: while (perspectivesIterator.hasNext()) {
            final IOCBeanDef<AbstractWorkbenchPerspectiveActivity> perspective = perspectivesIterator.next();
            final AbstractWorkbenchPerspectiveActivity instance = perspective.getInstance();
            if (instance.isDefault()) {
                defaultPerspective = instance;
                break outer_loop;
            } else {
                iocManager.destroyBean(instance);
            }
        }
        return defaultPerspective;
    }

    private List<AbstractWorkbenchPerspectiveActivity> getPerspectiveActivities() {

        // Get Perspective Providers
        final Set<AbstractWorkbenchPerspectiveActivity> activities = activityManager
                .getActivities(AbstractWorkbenchPerspectiveActivity.class);

        // Sort Perspective Providers so they're always in the same sequence!
        List<AbstractWorkbenchPerspectiveActivity> sortedActivities = new ArrayList<AbstractWorkbenchPerspectiveActivity>(
                activities);
        Collections.sort(sortedActivities, new Comparator<AbstractWorkbenchPerspectiveActivity>() {

            @Override
            public int compare(AbstractWorkbenchPerspectiveActivity o1, AbstractWorkbenchPerspectiveActivity o2) {
                return o1.getPerspective().getName().compareTo(o2.getPerspective().getName());
            }

        });

        return sortedActivities;
    }

    // Fade out the "Loading application" pop-up
    private void hideLoadingPopup() {
        final Element e = RootPanel.get("loading").getElement();

        new Animation() {
            @Override
            protected void onUpdate(double progress) {
                e.getStyle().setOpacity(1.0 - progress);
            }

            @Override
            protected void onComplete() {
                e.getStyle().setVisibility(Style.Visibility.HIDDEN);
            }
        }.run(500);
    }

    public static native void redirect(String url)/*-{
                                                  $wnd.location = url;
                                                  }-*/;
}
