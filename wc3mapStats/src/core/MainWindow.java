package core;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omg.CORBA_2_3.portable.OutputStream;

import javafx.application.Application.Parameters;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.moonlightflower.wc3libs.app.Minimap;
import net.moonlightflower.wc3libs.bin.app.MMP;
import net.moonlightflower.wc3libs.bin.app.SHD;
import net.moonlightflower.wc3libs.bin.app.W3E;
import net.moonlightflower.wc3libs.bin.app.W3I;
import net.moonlightflower.wc3libs.bin.app.WPM;
import net.moonlightflower.wc3libs.dataTypes.app.Coords2DI;
import net.moonlightflower.wc3libs.dataTypes.app.Wc3String;
import net.moonlightflower.wc3libs.misc.FieldId;
import net.moonlightflower.wc3libs.misc.Size;
import net.moonlightflower.wc3libs.misc.Translator;
import net.moonlightflower.wc3libs.misc.image.BLP;
import net.moonlightflower.wc3libs.misc.image.TGA;
import net.moonlightflower.wc3libs.misc.image.Wc3RasterImg;
import net.moonlightflower.wc3libs.port.LadikMpqPort;
import net.moonlightflower.wc3libs.port.MpqPort;
import net.moonlightflower.wc3libs.port.Orient;
import net.moonlightflower.wc3libs.txt.FDF;
import net.moonlightflower.wc3libs.txt.TXT;
import net.moonlightflower.wc3libs.txt.WTS;

public class MainWindow implements Initializable {
	@FXML
	private Label _mapFileLabel;
	@FXML
	private GridPane _imgGridPane;
	@FXML
	private Pane _previewBox;
	@FXML
	private ImageView _previewView;
	@FXML
	private Pane _minimapBox;
	@FXML
	private ImageView _minimapView;
	@FXML
	private Pane _pathingBox;
	@FXML
	private ImageView _pathingView;
	@FXML
	private Pane _shadowBox;
	@FXML
	private ImageView _shadowView;
	@FXML
	private Label _mapNameValLabel;
	@FXML
	private Label _mapAuthorValLabel;
	@FXML
	private Label _mapDescriptionValLabel;
	@FXML
	private Label _playersRecommendedValLabel;
	@FXML
	private Label _sizeValLabel;
	@FXML
	private Label _baseTilesetValLabel;
	@FXML
	private Label _savesAmountValLabel;
	@FXML
	private GridPane _flagsGrid;
	@FXML
	private VBox _forcesBox;
	
	@FXML
	private Button _trigsButton;
	@FXML
	private Button _objModsButton;
	
	private Stage _stage;
	private File _mapFile = null;
	
	private Translator _translator;
	private W3I _info;
	
	private TrigsWindow _trigsWindow = null;
	private ObjModsWindow _objModsWindow = null;
	
	public MainWindow(Stage primaryStage, Parameters params) throws IOException {
		if (params.getRaw().size() > 0) {
			_mapFile = new File(params.getRaw().get(0));
		}
		
		try {
			TXT settings = new TXT(new File(Orient.getExecDir(this.getClass()), "settings.conf"));
			
			Wc3String wc3dirS = (Wc3String) settings.get(FieldId.valueOf("wc3dir"));
			
			if ((_mapFile == null) && settings.containsKey(FieldId.valueOf("debug"))) _mapFile = new File("D:\\Warcraft III\\Maps\\Download\\Battle of Flags v2.0a~3.w3x");

			File wc3dir = MpqPort.getWc3Dir();

			if (wc3dirS != null) {
				wc3dir = new File(wc3dirS.toString());
				
				MpqPort.setWc3Dir(wc3dir);
			}
			
			if (wc3dir == null) throw new Exception("wc3 directory is unknown");
			
			System.out.println(String.format("wc3dir=%s", wc3dir));
			
			if (_mapFile == null) throw new IllegalArgumentException("got no mapFile, write \"java -jar wc3mapStats.jar <mapFile>\"");
			
			System.out.println(String.format("map=%s", _mapFile));
			
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/core/MainWindow.fxml"));
			
			fxmlLoader.setController(this);

			Parent root = fxmlLoader.load();

			Scene scene = new Scene(root);
			
			_stage = primaryStage;
			
			_stage.setScene(scene);
			_stage.setTitle("wc3mapStats");
			_stage.show();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class ImageHost extends StackPane {
		private ChangeListener<Number> _widthListener;
		private ChangeListener<Number> _heightListener;
		
		private Text _defaultText;
		
		public void setImage(ImageView imgView) {
			getChildren().clear();

			getChildren().add((imgView == null) ? _defaultText : imgView);
		}
		
		public ImageHost(Text defaultText) {
			_defaultText = defaultText;
			
			setStyle("-fx-background-color:#FFFFFF;");
			setAlignment(Pos.CENTER);
			
			parentProperty().addListener(new ChangeListener<Parent>() {
				@Override
				public void changed(ObservableValue<? extends Parent> obs, Parent oldVal, Parent newVal) {
					if (oldVal != null) {
						Region oldRegion = (Region) oldVal;
						
						if (oldRegion != null) {
							oldRegion.widthProperty().removeListener(_widthListener);
							oldRegion.widthProperty().removeListener(_heightListener);
						}
					}
					
					Region newRegion = (Region) newVal;

					if (newRegion == null) return;
					
					_widthListener = new ChangeListener<Number>() {
						@Override
						public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
							if (newRegion.widthProperty().get() < newRegion.heightProperty().get()) {
								prefWidthProperty().bind(newRegion.widthProperty());
								prefHeightProperty().bind(newRegion.widthProperty());
							} else {
								prefWidthProperty().bind(newRegion.heightProperty());
								prefHeightProperty().bind(newRegion.heightProperty());
							}
						}
					};
					_heightListener = new ChangeListener<Number>() {
						@Override
						public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
							if (newRegion.widthProperty().get() < newRegion.heightProperty().get()) {
								prefWidthProperty().bind(newRegion.widthProperty());
								prefHeightProperty().bind(newRegion.widthProperty());
							} else {
								prefWidthProperty().bind(newRegion.heightProperty());
								prefHeightProperty().bind(newRegion.heightProperty());
							}
						}
					};
					
					newRegion.widthProperty().addListener(_widthListener);
					newRegion.widthProperty().addListener(_heightListener);
				}
			});
			
			getChildren().add(defaultText);
		}
	}

	public void setPreview() {
		MpqPort.Out portOut = new LadikMpqPort.Out();
		
		portOut.add(Minimap.PREVIEW_TGA_GAME_PATH);
		
		ImageHost imgHost = new ImageHost(new Text("<no preview>"));
		
		_previewBox.getChildren().clear();
		_previewBox.getChildren().add(imgHost);

		try {
			MpqPort.Out.Result portResult = portOut.commit(_mapFile);
			
			if (!portResult.getExports().containsKey(Minimap.PREVIEW_TGA_GAME_PATH)) return; 

			/*byte[] bytes = portResult.getExports().get(Minimap.PREVIEW_TGA_GAME_PATH).getOutBytes();
			
			FileOutputStream outStream = new FileOutputStream(new File("D:\\outPreview.tga"));
			
			for (int i = 0; i < bytes.length; i++) {
				outStream.write(bytes[i]);
			}
			
			outStream.close();*/
			
			File previewFile = portResult.getFile(Minimap.PREVIEW_TGA_GAME_PATH);
			
			Wc3RasterImg previewImg = Wc3RasterImg.ofFile(previewFile);
			
			previewImg.ignoreAlpha();
			
			_previewView.setImage(previewImg.getFXImg());

			imgHost.setImage(_previewView);
		} catch (Exception e) {
			e.printStackTrace();
			imgHost.setImage(null);
		}
		
		_previewView.fitWidthProperty().bind(_previewBox.widthProperty());
		_previewView.fitHeightProperty().bind(_previewBox.heightProperty());
	}

	public void setMinimap() {
		MpqPort.Out portOut = new LadikMpqPort.Out();
		
		portOut.add(Minimap.BACKGROUND_BLP_GAME_PATH);

		ImageHost imgHost = new ImageHost(new Text("<no minimap>"));
		
		_minimapBox.getChildren().clear();
		_minimapBox.getChildren().add(imgHost);
		
		try {
			MpqPort.Out.Result portResult = portOut.commit(_mapFile);
			
			if (!portResult.getExports().containsKey(Minimap.BACKGROUND_BLP_GAME_PATH)) return;
			
			Wc3RasterImg minimapImg = Wc3RasterImg.ofFile(portResult.getFile(Minimap.BACKGROUND_BLP_GAME_PATH));
			
			MMP mmp = MMP.ofMapFile(_mapFile);
			
			Wc3RasterImg mmpImg = mmp.toImg();
			
			minimapImg.merge(mmpImg, new Coords2DI(0, 0), true);
			
			minimapImg.ignoreAlpha();
			
			if (_info.getFlag(W3I.Flags.Flag.HIDE_MINIMAP)) {
				File unknownMinimapPath = new File("UI\\Widgets\\Glues\\Minimap-Unknown.blp");
				
				MpqPort.Out.Result gamePortResult = new LadikMpqPort().getGameFiles(unknownMinimapPath);
				
				_minimapView.setImage(new BLP(gamePortResult.getInputStream(unknownMinimapPath)).getFXImg());
			} else {
				System.out.println("C");
				_minimapView.setImage(minimapImg.getFXImg());
			}
			
			imgHost.setImage(_minimapView);
		} catch (Exception e) {
			imgHost.setImage(null);
		}
		
		_minimapView.fitWidthProperty().bind(_minimapBox.widthProperty());
		_minimapView.fitHeightProperty().bind(_minimapBox.heightProperty());
	}

	public void setPathing() {
		ImageHost imgHost = new ImageHost(new Text("<no pathing>"));
		
		_pathingBox.getChildren().clear();
		_pathingBox.getChildren().add(imgHost);
		
		try {
			WPM wpm = WPM.ofMap(_mapFile);
			
			Wc3RasterImg img = wpm.getPathMap().toImg();
			
			Size size = new Size(Math.max(img.getWidth(), img.getHeight()), Math.max(img.getWidth(), img.getHeight()));
			
			img.enlarge(size, Color.BLACK);
			
			_pathingView.setImage(img.getFXImg());
			
			imgHost.setImage(_pathingView);
		} catch (IOException e) {
			imgHost.setImage(null);
		}
		
		_pathingView.fitWidthProperty().bind(_pathingBox.widthProperty());
		_pathingView.fitHeightProperty().bind(_pathingBox.heightProperty());
	}
	
	public void setShadow() {
		ImageHost imgHost = new ImageHost(new Text("<no shadow>"));
		
		_shadowBox.getChildren().clear();
		_shadowBox.getChildren().add(imgHost);
		
		try {
			SHD shd = SHD.ofMap(_mapFile);
			
			shd.getShadowMap().setBoundsByWorld(_info.getWorldBounds().scale(W3E.CELL_SIZE), true, false);
			
			Wc3RasterImg img = shd.getShadowMap().toImg();
			
			Size size = new Size(Math.max(img.getWidth(), img.getHeight()), Math.max(img.getWidth(), img.getHeight()));
			
			img.enlarge(size, Color.BLACK);
			
			_shadowView.setImage(img.getFXImg());
			
			imgHost.setImage(_shadowView);
		} catch (IOException e) {
			imgHost.setImage(null);
		}
		
		_shadowView.fitWidthProperty().bind(_shadowBox.widthProperty());
		_shadowView.fitHeightProperty().bind(_shadowBox.heightProperty());
	}

	private class PlayerRow {
		private W3I.Player _player;
		
		public StringProperty _nameProperty = new SimpleStringProperty();
		public StringProperty _controlProperty = new SimpleStringProperty();
		public StringProperty _raceProperty = new SimpleStringProperty();
		
		public PlayerRow(W3I.Player player) {
			_player = player;
			
			_nameProperty.set(_translator.translate(player.getName()));
			_controlProperty.set(player.getType().toString());			
			_raceProperty.set(_translator.translate(player.getRace().toString()));
		}
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		_mapFileLabel.setText(_mapFile.toString());

		try {			
			_info = W3I.ofMapFile(_mapFile);
			
			setPreview();
			setMinimap();
			setPathing();
			setShadow();

			_translator = new Translator();
			
			try {
				WTS wts = WTS.ofMapFile(_mapFile);
				
				_translator.addTXT(wts.toTXT());
			} catch (Exception e) {
				System.err.println("warning: no wts");
			}
			
			FDF fdf = FDF.ofGameFile(new File("UI\\FrameDef\\GlobalStrings.fdf"));
			
			TXT worldEditGameStringsTXT = TXT.ofGameFile(new File("UI\\WorldEditGameStrings.txt"));
			
			_translator.addTXT(fdf.toTXT());
			_translator.addTXT(worldEditGameStringsTXT);
			
			_mapNameValLabel.setText(_translator.translate(_info.getMapName()));
			_mapAuthorValLabel.setText(_translator.translate(_info.getMapAuthor()));
			_mapDescriptionValLabel.setText(_translator.translate(_info.getMapDescription()));
			_playersRecommendedValLabel.setText(_translator.translate(_info.getPlayersRecommendedAmount()));
			_sizeValLabel.setText(String.format("%dx%d / left=%d right=%d bottom=%d top=%d", _info.getWorldBounds().getSize().getWidth(), _info.getWorldBounds().getSize().getHeight(), _info.getMarginLeft(), _info.getMarginRight(), _info.getMarginBottom(), _info.getMarginTop()));
			_baseTilesetValLabel.setText(_translator.translate(_info.getTileset().toString()));
			_savesAmountValLabel.setText(String.format("%d / %d", _info.getSavesAmount(), _info.getEditorVersion()));
			
			for (W3I.Flags.Flag flag : W3I.Flags.Flag.values()) {
				Label nameLabel = new Label(flag.toString());
				Label valLabel = new Label(((Boolean) (_info.getFlags().containsFlag(flag))).toString());
				
				int row = _flagsGrid.impl_getRowCount();
				
				_flagsGrid.add(nameLabel, 0, row);
				_flagsGrid.add(valLabel, 1, row);
			}
			
			for (W3I.Force force : _info.getForces()) {
				VBox forcePanel = new VBox();
				
				VBox.setVgrow(forcePanel, Priority.ALWAYS);
				
				Label nameLabel = new Label(_translator.translate(force.getName()));
				
				forcePanel.getChildren().add(nameLabel);
				
				TableView<PlayerRow> tableView = new TableView<>();
				
				tableView.getColumns().clear();
				
				TableColumn<PlayerRow, String> playerNameCol = new TableColumn<>("name");
				TableColumn<PlayerRow, String> playerControlCol = new TableColumn<>("controller");
				TableColumn<PlayerRow, String> playerRaceCol = new TableColumn<>("race");
				
				tableView.getColumns().add(playerNameCol);
				tableView.getColumns().add(playerControlCol);
				tableView.getColumns().add(playerRaceCol);
				
				tableView.setEditable(false);
				tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
				
				for (TableColumn<PlayerRow, ?> col : tableView.getColumns()) {
					/*col.tableViewProperty().addListener(new ChangeListener<TableView<PlayerRow>>() {
						@Override
						public void changed(ObservableValue<? extends TableView<PlayerRow>> obsVal, TableView<PlayerRow> oldVal, TableView<PlayerRow> newVal) {
							if (col.prefWidthProperty().isBound()) {
								col.prefWidthProperty().unbind();
							}
							
							col.prefWidthProperty().bind(newVal.widthProperty().multiply(0.33));
						}
					});*/
					
					if (tableView.getColumns().get(tableView.getColumns().size() - 1) == col) {
						//col.prefWidthProperty().bind(tableView.widthProperty().multiply(0.34));
					} else {
						col.prefWidthProperty().bind(tableView.widthProperty().multiply(0.33));
					}
					
					col.impl_setReorderable(false);
					col.setSortable(false);
				}
				
				playerNameCol.setCellValueFactory(new Callback<CellDataFeatures<PlayerRow, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<PlayerRow, String> param) {
						return param.getValue()._nameProperty;
					}
				});
				playerControlCol.setCellValueFactory(new Callback<CellDataFeatures<PlayerRow, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<PlayerRow, String> param) {
						return param.getValue()._controlProperty;
					}
				});
				playerRaceCol.setCellValueFactory(new Callback<CellDataFeatures<PlayerRow, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<PlayerRow, String> param) {
						return param.getValue()._raceProperty;
					}
				});
				
				for (int i = 0; i < 12; i++) {
					if (((force.getPlayers() >> i) & 0x1) == 0) continue;
					
					W3I.Player p = _info.getPlayerFromNum(i);
					
					if (p == null) continue;
					
					PlayerRow row = new PlayerRow(p);

					tableView.getItems().add(row);
				}
				
				forcePanel.getChildren().add(tableView);
				
				VBox.setVgrow(tableView, Priority.ALWAYS);
				
				_forcesBox.getChildren().add(forcePanel);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		_trigsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					if (_trigsWindow == null) {
						_trigsWindow = new TrigsWindow(_mapFile);
					}
					
					_trigsWindow.show();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		_objModsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					if (_objModsWindow == null) {
						_objModsWindow = new ObjModsWindow(_mapFile, _translator);
					}
					
					_objModsWindow.show();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
}
