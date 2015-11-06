/*******************************************************************************
 * Copyright 2015 Esri
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package com.esri.defensese.basicmapquartzbeta1;

import com.esri.arcgisruntime.security.UserCredential;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * A controller class for the login dialog.
 */
public class LoginDialogController {
    
    @FXML
    private TextField textField_username;
    
    @FXML
    private PasswordField passwordField_password;
    
    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            textField_username.requestFocus();
        });
    }
    
    /**
     * Returns a UserCredential based on the values of the login dialog's TextField
     * and PasswordField.
     * @return a UserCredential based on the values of the login dialog's TextField
     *         and PasswordField.
     */
    public UserCredential getUserCredential() {
        return new UserCredential(textField_username.getText(), passwordField_password.getText());
    }
    
}
