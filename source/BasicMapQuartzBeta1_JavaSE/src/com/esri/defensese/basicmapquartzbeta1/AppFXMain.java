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

import com.esri.arcgisruntime.datasource.arcgis.ArcGISFeatureTable;
import com.esri.arcgisruntime.datasource.arcgis.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.EnvelopeBuilder;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.loadable.LoadStatusChangedEvent;
import com.esri.arcgisruntime.loadable.LoadStatusChangedListener;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Map;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.security.AuthenticationChallenge;
import com.esri.arcgisruntime.security.AuthenticationChallengeAction;
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler;
import com.esri.arcgisruntime.security.AuthenticationChallengeResponse;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.UserCredential;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * An app that displays a map with a feature service, demonstrating they key themes
 * of ArcGIS Runtime Quartz Beta 1.
 */
public class AppFXMain extends Application {

    private static final Logger logger = Logger.getLogger(AppFXMain.class.getName());

    private final MapView mapView;
    private final Map map;
    private final Label layerStatusLabel = new Label();
    private final Label srLabel = new Label();
    
    private CountDownLatch loginLatch = null;
    private UserCredential credential = null;
    
    /**
     * Instantiates the app and creates and displays the map.
     */
    public AppFXMain() {
        //Unsecured feature service
        String featureServiceUrl = "http://services.arcgis.com/hRUr1F8lE8Jq2uJo/arcgis/rest/services/KFSD_Fire_Stations/FeatureServer/0";
        String definitionExpression = "1 = 1";

        /**
         * *********************************************************************
         * 4. Centralized handling of authentication
         */
        //Secured feature service
        featureServiceUrl = "https://services1.arcgis.com/63cSRCcqLtJKDSR2/arcgis/rest/services/nhsvc_sites/FeatureServer/0";
        definitionExpression = "Name LIKE '%Sa%'";
        AuthenticationManager.setAuthenticationChallengeHandler(new AuthenticationChallengeHandler() {

            @Override
            public AuthenticationChallengeResponse handleChallenge(AuthenticationChallenge challenge) {
                AuthenticationChallengeResponse response = null;
                switch (challenge.getType()) {
                    case USER_CREDENTIAL_CHALLENGE:
                        loginLatch = new CountDownLatch(1);
                        credential = null;
                        Platform.runLater(() -> {
                            try {
                                FXMLLoader loginDialogLoader = new FXMLLoader(AppFXMain.this.getClass().getResource("LoginDialog.fxml"));
                                DialogPane dialogPane = loginDialogLoader.load();
                                Dialog<ButtonType> loginDialog = new Dialog<>();
                                loginDialog.setDialogPane(dialogPane);
                                ButtonType buttonType  = loginDialog.showAndWait().get();
                                if (ButtonType.OK.equals(buttonType)) {
                                    LoginDialogController controller = loginDialogLoader.getController();
                                    credential = controller.getUserCredential();
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(AppFXMain.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            loginLatch.countDown();
                        });
                        try {
                            loginLatch.await();
                            if (null != credential) {
                                response = new AuthenticationChallengeResponse(AuthenticationChallengeAction.CONTINUE_WITH_CREDENTIAL, credential);
                            } else {
                                response = new AuthenticationChallengeResponse(AuthenticationChallengeAction.CANCEL, "No credentials entered by user");
                            }
                        } catch (InterruptedException ex) {
                            Logger.getLogger(AppFXMain.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;

                    case CERTIFICATE_CHALLENGE:
                    case OAUTH_CREDENTIAL_CHALLENGE:
                    case SELF_SIGNED_CHALLENGE:
                    case UNKNOWN:
                    default:
                        //TODO notify the user that this auth type is not supported
                        //     by this app
                }
                return response;
            }
        });

        /**
         * *********************************************************************
         * 1. Runtime common API
         * 2. New mapping API
         */
        mapView = new MapView();

        this.map = new Map();
        map.setBasemap(Basemap.createTopographic());

        ArcGISFeatureTable featureTable = new ServiceFeatureTable(featureServiceUrl);
        final FeatureLayer featureLayer = new FeatureLayer(featureTable);
        featureLayer.setDefinitionExpression(definitionExpression);
        map.getOperationalLayers().add(featureLayer);

        /**
         * *********************************************************************
         * 3. Loadable pattern
         */
        layerStatusLabel.setText("Layer: " + LoadStatus.NOT_LOADED);
        featureLayer.addLoadStatusChangedListener(new LoadStatusChangedListener() {
            @Override
            public void loadStatusChanged(LoadStatusChangedEvent evt) {
                LoadStatus newLoadStatus = evt.getNewLoadStatus();
                Platform.runLater(() -> {
                    layerStatusLabel.setText("Layer: " + newLoadStatus.toString());
                });
                if (LoadStatus.LOADED.equals(newLoadStatus)) {
                    Envelope fullExtent = featureLayer.getFullExtent();
                    
                    /**
                     * *********************************************************
                     * 5. Geometry objects
                     */
                    EnvelopeBuilder envBuilder = new EnvelopeBuilder(fullExtent);
                    double halfWidth = envBuilder.getWidth() / 2.0;
                    double halfHeight = envBuilder.getHeight() / 2.0;
                    envBuilder.setXMin(envBuilder.getXMin() - halfWidth);
                    envBuilder.setXMax(envBuilder.getXMax() + halfWidth);
                    envBuilder.setYMin(envBuilder.getYMin() - halfHeight);
                    envBuilder.setYMax(envBuilder.getYMax() + halfHeight);
                    fullExtent = envBuilder.toGeometry();
                    logger.log(Level.INFO,
                            "By the way, the spatial reference of this geometry is "
                                    + fullExtent.getSpatialReference().getWKText()
                                    + ". Try doing THAT with ArcGIS Runtime 10.2.x!");

                    mapView.setViewpointGeometryAsync(fullExtent);
                }
            }
        });
        srLabel.setText("SR: " + getSpatialReferenceString(map));
        map.addDoneLoadingListener(new Runnable() {
            public void run() {
                Platform.runLater(() -> {
                    srLabel.setText("SR: " + getSpatialReferenceString(map));
                });
            }
        });

        mapView.setMap(map);
    }

    private static String getSpatialReferenceString(Map map) {
        return (null == map.getSpatialReference() ? "null" : Integer.toString(map.getSpatialReference().getWKID()));
    }

    /**
     * Creates and displays the app.
     * @param primaryStage the Stage on which to display the app.
     */
    @Override
    public void start(Stage primaryStage) {
        layerStatusLabel.getStyleClass().setAll("label-over-map");
        srLabel.getStyleClass().setAll("label-over-map");
        StackPane root = new StackPane();
        root.getChildren().add(mapView);
        root.getChildren().add(layerStatusLabel);
        StackPane.setAlignment(layerStatusLabel, Pos.TOP_LEFT);
        root.getChildren().add(srLabel);
        StackPane.setAlignment(srLabel, Pos.BOTTOM_LEFT);

        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add(AppFXMain.class.getResource("styles.css").toExternalForm());

        primaryStage.setTitle("Basic Map--ArcGIS Runtime Quartz Beta 1");
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Disposes the Map and MapView, then ends the app by calling System.exit(0).
     */
    @Override
    public void stop() {
        map.dispose();
        mapView.dispose();
        System.exit(0);
    }

    /**
     * @param args the command line arguments (not used in this app).
     */
    public static void main(String[] args) {
        launch(args);
    }

}
