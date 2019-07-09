package com.airbnb.android.react.maps;

import android.content.Context;
import android.content.res.AssetManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.lang.IllegalArgumentException;

public class AirMapLocalTile extends AirMapFeature {

    class AIRMapLocalTileProvider implements TileProvider {
        private static final int BUFFER_SIZE = 16 * 1024;
        private int tileSize;
        private String pathTemplate;
				private String urlScope = "assets";
				private AssetManager mAssets;

				public AIRMapLocalTileProvider(int tileSizet, String pathTemplate, String urlScope, AssetManager assets) {
						mAssets = assets;
						this.tileSize = tileSizet;
						this.pathTemplate = pathTemplate;
						this.urlScope = urlScope;
				}

        @Override
        public Tile getTile(int x, int y, int zoom) {
            byte[] image = readTileImage(x, y, zoom);
            return image == null ? TileProvider.NO_TILE : new Tile(this.tileSize, this.tileSize, image);
        }

        public void setPathTemplate(String pathTemplate) {
            this.pathTemplate = pathTemplate;
        }

        public void setTileSize(int tileSize) {
            this.tileSize = tileSize;
        }

				public void setUrlScope(String urlScope) {
					this.urlScope = urlScope;
				}

        private byte[] readTileImage(int x, int y, int zoom) {
      			InputStream in = null;
        		ByteArrayOutputStream buffer = null;
						System.out.println(this.urlScope);

						try {
								switch (this.urlScope) {
									case "assets": // by default
										in = mAssets.open(getTileFilename(x, y, zoom));
										break;
									case "fs":
										File file = new File(getTileFilename(x, y, zoom));
										in = new FileInputStream(file);
										break;
									default:
										throw new IllegalArgumentException("You have to specify an urlScope");
								}
								
								buffer = new ByteArrayOutputStream();

								int nRead;
								byte[] data = new byte[BUFFER_SIZE];

								while ((nRead = in.read(data, 0, BUFFER_SIZE)) != -1) {
										buffer.write(data, 0, nRead);
								}
								buffer.flush();

								return buffer.toByteArray();
						} catch (IOException e) {
								e.printStackTrace();
								return null;
						} catch (OutOfMemoryError e) {
								e.printStackTrace();
								return null;
						} catch (IllegalArgumentException e) {
								e.printStackTrace();
								return null;
						} finally {
								if (in != null) try { in.close(); } catch (Exception ignored) {}
								if (buffer != null) try { buffer.close(); } catch (Exception ignored) {}
						}
        }

        private String getTileFilename(int x, int y, int zoom) {
            String s = this.pathTemplate
                    .replace("{x}", Integer.toString(x))
                    .replace("{y}", Integer.toString(y))
                    .replace("{z}", Integer.toString(zoom));
            return s;
        }
    }

    private TileOverlayOptions tileOverlayOptions;
    private TileOverlay tileOverlay;
    private AirMapLocalTile.AIRMapLocalTileProvider tileProvider;

    private String pathTemplate;
		private String urlScope;
    private float tileSize;
    private float zIndex;

    public AirMapLocalTile(Context context) {
        super(context);
    }

    public void setPathTemplate(String pathTemplate) {
        this.pathTemplate = pathTemplate;
        if (tileProvider != null) {
            tileProvider.setPathTemplate(pathTemplate);
        }
        if (tileOverlay != null) {
            tileOverlay.clearTileCache();
        }
    }

    public void setZIndex(float zIndex) {
        this.zIndex = zIndex;
        if (tileOverlay != null) {
            tileOverlay.setZIndex(zIndex);
        }
    }

    public void setTileSize(float tileSize) {
        this.tileSize = tileSize;
        if (tileProvider != null) {
            tileProvider.setTileSize((int)tileSize);
        }
    }

		public void setUrlScope(String urlScope) {
			this.urlScope = urlScope;
			if (tileProvider != null) {
				tileProvider.setUrlScope(urlScope);
			}
		}

    public TileOverlayOptions getTileOverlayOptions() {
        if (tileOverlayOptions == null) {
            tileOverlayOptions = createTileOverlayOptions();
        }
        return tileOverlayOptions;
    }

    private TileOverlayOptions createTileOverlayOptions() {
        TileOverlayOptions options = new TileOverlayOptions();
        options.zIndex(zIndex);
        this.tileProvider = new AirMapLocalTile.AIRMapLocalTileProvider((int)this.tileSize, this.pathTemplate, this.urlScope, getResources().getAssets());
        options.tileProvider(this.tileProvider);
        return options;
    }

    @Override
    public Object getFeature() {
        return tileOverlay;
    }

    @Override
    public void addToMap(GoogleMap map) {
        this.tileOverlay = map.addTileOverlay(getTileOverlayOptions());
    }

    @Override
    public void removeFromMap(GoogleMap map) {
        tileOverlay.remove();
    }
}
