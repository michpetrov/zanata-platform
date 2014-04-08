/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.page.dashboard;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.google.common.base.Predicate;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class DashboardSettingsTab extends DashboardBasePage {
    
    @FindBy(id = "emailUpdateForm:emailField:email")
    private WebElement emailField;

    @FindBy(id = "updateEmailButton")
    private WebElement updateEmailButton;
    
    @FindBy(id = "passwordChangeForm:oldPasswordField:oldPassword")
    private WebElement oldPasswordField;
    
    @FindBy(id = "passwordChangeForm:newPasswordField:newPassword")
    private WebElement newPasswordField;
    
    @FindBy(id = "changePasswordButton")
    private WebElement changePasswordButton;
    
    public DashboardSettingsTab(WebDriver driver) {
        super(driver);
    }

    public DashboardSettingsTab typeNewAccountEmailAddress(String emailAddress) {
        emailField.clear();
        emailField.sendKeys(emailAddress);
        return this;
    }

    public DashboardSettingsTab clickUpdateEmailButton() {
        updateEmailButton.click();
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return !DashboardSettingsTab.this.getNotificationMessage()
                        .contains("admin");
            }
        });
        return this;
    }

    public DashboardSettingsTab typeOldPassword(String oldPassword) {
        oldPasswordField.clear();
        oldPasswordField.sendKeys(oldPassword);
        return this;
    }

    public DashboardSettingsTab typeNewPassword(String newPassword) {
        newPasswordField.clear();
        newPasswordField.sendKeys(newPassword);
        return this;
    }

    public DashboardSettingsTab clickUpdatePasswordButton() {
        changePasswordButton.click();
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return !DashboardSettingsTab.this.getNotificationMessage()
                        .contains("admin");
            }
        });
        return this;
    }
}
