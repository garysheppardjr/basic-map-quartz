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
package com.esri.defensese.basicmapquartz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

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

import java.util.concurrent.CountDownLatch;

/**
 * An app that displays a map with a feature service, demonstrating they key themes
 * of ArcGIS Runtime Quartz.
 */
public class BasicMapQuartzActivity extends AppCompatActivity {

    private static final int REQUEST_LOGIN = 1;

    private MapView mapView = null;
    private Map map = null;
    private TextView layerStatusLabel = null;
    private TextView srLabel = null;

    private CountDownLatch loginLatch = null;
    private UserCredential credential = null;

    /**
     * Creates the UI and creates and displays the map.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_basic_map_quartz);

        layerStatusLabel = (TextView) findViewById(R.id.textView_layerStatus);
        srLabel = (TextView) findViewById(R.id.textView_spatialReference);

        //Unsecured feature service
        String featureServiceUrl = "http://services.arcgis.com/hRUr1F8lE8Jq2uJo/arcgis/rest/services/KFSD_Fire_Stations/FeatureServer/0";
        String definitionExpression = "1 = 1";

        /**
         * *********************************************************************
         * 4. Centralized handling of authentication
         */
        //Secured feature service
        featureServiceUrl = "http://services1.arcgis.com/63cSRCcqLtJKDSR2/arcgis/rest/services/nhsvc_sites/FeatureServer/0";
        definitionExpression = "Name LIKE '%Sa%'";
        AuthenticationManager.setAuthenticationChallengeHandler(new AuthenticationChallengeHandler() {

            @Override
            public AuthenticationChallengeResponse handleChallenge(AuthenticationChallenge challenge) {
                AuthenticationChallengeResponse response = null;
                switch (challenge.getType()) {
                    case USER_CREDENTIAL_CHALLENGE:
                        loginLatch = new CountDownLatch(1);
                        credential = null;
                        Intent intent = new Intent();
                        intent.setClass(BasicMapQuartzActivity.this, LoginActivity.class);
                        intent.putExtra(LoginActivity.EXTRA_HOSTNAME, challenge.getServiceHostname());
                        startActivityForResult(intent, REQUEST_LOGIN);
                        try {
                            loginLatch.await();
                            if (null != credential) {
                                response = new AuthenticationChallengeResponse(AuthenticationChallengeAction.CONTINUE_WITH_CREDENTIAL, credential);
                            } else {
                                response = new AuthenticationChallengeResponse(AuthenticationChallengeAction.CANCEL, "No credentials entered by user");
                            }
                        } catch (InterruptedException ex) {
                            Log.e(BasicMapQuartzActivity.this.getClass().getSimpleName(), null, ex);
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
        mapView = (MapView) findViewById(R.id.mapView);

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
                layerStatusLabel.setText("Layer: " + newLoadStatus.toString());
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
                    Log.i(getClass().getSimpleName(),
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
                srLabel.setText("SR: " + getSpatialReferenceString(map));
            }
        });

        mapView.setMap(map);
    }

    private static String getSpatialReferenceString(Map map) {
        return (null == map.getSpatialReference() ? "null" : Integer.toString(map.getSpatialReference().getWKID()));
    }

    /**
     * Handles the result of a started Activity.
     * @param requestCode the request code. If it's REQUEST_LOGIN, this method creates a UserCredential
     *                    object.
     * @param resultCode the result code.
     * @param data the Intent returned by the activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_LOGIN == requestCode) {
            credential = new UserCredential(
                    data.getStringExtra(LoginActivity.EXTRA_USERNAME),
                    data.getStringExtra(LoginActivity.EXTRA_PASSWORD)
            );
            if (null != loginLatch) {
                loginLatch.countDown();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Pauses the MapView and then calls super.onPause().
     */
    @Override
    protected void onPause(){
        if (null != mapView) {
            mapView.pause();
        }
        super.onPause();
    }

    /**
     * Calls super.onResume() and then resumes the MapView.
     */
    @Override
    protected void onResume(){
        super.onResume();
        if (null != mapView) {
            mapView.resume();
        }
    }

    /**
     * Inflates the options menu.
     * @param menu the Menu.
     * @return true on success.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_basic_map_quartz, menu);
        return true;
    }

    /**
     * Handles the selection of an options menu item.
     * @param item the selected MenuItem.
     * @return true on success.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
