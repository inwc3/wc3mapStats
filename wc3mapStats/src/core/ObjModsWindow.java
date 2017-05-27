package core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.moonlightflower.wc3libs.bin.ObjMod;
import net.moonlightflower.wc3libs.bin.app.W3I;
import net.moonlightflower.wc3libs.bin.app.WCT;
import net.moonlightflower.wc3libs.bin.app.WTG;
import net.moonlightflower.wc3libs.bin.app.WTG.FuncCat;
import net.moonlightflower.wc3libs.bin.app.WTG.Trig.ECA;
import net.moonlightflower.wc3libs.bin.app.objMod.W3A;
import net.moonlightflower.wc3libs.bin.app.objMod.W3B;
import net.moonlightflower.wc3libs.bin.app.objMod.W3D;
import net.moonlightflower.wc3libs.bin.app.objMod.W3H;
import net.moonlightflower.wc3libs.bin.app.objMod.W3Q;
import net.moonlightflower.wc3libs.bin.app.objMod.W3T;
import net.moonlightflower.wc3libs.bin.app.objMod.W3U;
import net.moonlightflower.wc3libs.misc.FieldId;
import net.moonlightflower.wc3libs.misc.Translator;
import net.moonlightflower.wc3libs.misc.UnsupportedFormatException;
import net.moonlightflower.wc3libs.misc.image.Wc3RasterImg;
import net.moonlightflower.wc3libs.port.MpqPort;
import net.moonlightflower.wc3libs.port.Orient;
import net.moonlightflower.wc3libs.txt.Profile.Obj;

public class ObjModsWindow implements Initializable {
	@FXML
	private TitledPane _abilitiesPane;
	@FXML
	private TitledPane _buffsPane;
	@FXML
	private TitledPane _destructablesPane;
	@FXML
	private TitledPane _doodadsPane;
	@FXML
	private TitledPane _itemsPane;
	@FXML
	private TitledPane _unitsPane;
	@FXML
	private TitledPane _upgradesPane;
	
	@FXML
	private TableView<AttributeRow> _detailTable;
	
	private W3A _w3a = null;
	private W3H _w3h = null;
	private W3D _w3d = null;
	private W3B _w3b = null;
	private W3T _w3t = null;
	private W3U _w3u = null;
	private W3Q _w3q = null;
	
	private File _mapFile;
	private Translator _translator;
	
	private Stage _stage;
	
	public ObjModsWindow(File mapFile, Translator translator) throws IOException {
		_mapFile = mapFile;
		_translator = translator;

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/core/ObjModsWindow.fxml"));
		
		fxmlLoader.setController(this);

		Parent root = fxmlLoader.load();

		Scene scene = new Scene(root);
		
		_stage = new Stage();
		
		_stage.setScene(scene);
		_stage.setTitle("wc3mapStats - ObjMods");
	}
	
	public void show() {
		_stage.show();
		_stage.requestFocus();
	}

	/*private class DetailVal<T> {
		private T _val;
		
		public T getVal() {
			return _val;
		}
		
		@Override
		public String toString() {
			return getVal().toString();
		}
		
		public DetailVal(T val) {
			_val = val;
		}
	}

	private Map<File, Image> _icons = new HashMap<>();
	
	private Image fetchIcon(File file) {		
		if (Orient.getFileExt(file) == null) {
			file = new File(file.toString() + ".blp");
		}

		if (_icons.containsKey(file)) return _icons.get(file);
		
		try {
			MpqPort.Out.Result portResult = MpqPort.getDefaultImpl().getGameFiles(file);
			
			Image img = Wc3RasterImg.ofFile(portResult.getFile(file)).getFXImg();
			
			_icons.put(file, img);
			
			return img;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	private TreeItem<DetailVal<?>> addParam(WTG.Trig.ECA.Param param) {
		TreeItem<DetailVal<?>> paramItem = new TreeItem<>(new DetailVal<>(param));
		
		paramItem.setGraphic(new ImageView(fetchIcon(new File("ReplaceableTextures\\WorldEditUI\\Editor-TriggerFunction.blp"))));
		
		for (WTG.Trig.ECA.Param subParam : param.getParams()) {
			paramItem.getChildren().add(addParam(subParam));
		}
		
		paramItem.setExpanded(true);
		
		return paramItem;
	}
	
	private TreeItem<DetailVal<?>> addECA(WTG.Trig.ECA eca) {		
		TreeItem<DetailVal<?>> ecaItem = new TreeItem<>(new DetailVal<>(eca));
		
		ecaItem.setExpanded(true);

		TreeItem<DetailVal<?>> eventsItem = new TreeItem<>(new DetailVal<>("events"));
		TreeItem<DetailVal<?>> conditionsItem = new TreeItem<>(new DetailVal<>("conditions"));
		TreeItem<DetailVal<?>> actionsItem = new TreeItem<>(new DetailVal<>("actions"));

		eventsItem.setGraphic(new ImageView(fetchIcon(new File("ReplaceableTextures\\WorldEditUI\\Editor-TriggerEvent.blp"))));
		conditionsItem.setGraphic(new ImageView(fetchIcon(new File("ReplaceableTextures\\WorldEditUI\\Editor-TriggerCondition.blp"))));
		actionsItem.setGraphic(new ImageView(fetchIcon(new File("ReplaceableTextures\\WorldEditUI\\Editor-TriggerAction.blp"))));
		
		eventsItem.setExpanded(true);
		conditionsItem.setExpanded(true);
		actionsItem.setExpanded(true);

		String funcCatS = eca.getFunc().getCat();

		if (funcCatS != null) {
			FuncCat funcCat = _wtg.getFuncCats().get(FieldId.valueOf(funcCatS));

			if (funcCat != null) {
				File iconFile = funcCat.getIconFile();

				if (iconFile != null) {
					ecaItem.setGraphic(new ImageView(fetchIcon(iconFile)));
				}
			}
		}
		
		for (WTG.Trig.ECA.Param param : eca.getParams()) {
			ecaItem.getChildren().add(addParam(param));
		}
		
		for (WTG.Trig.ECA subECA : eca.getECAs()) {
			Map<WTG.Trig.ECA.ECAType, TreeItem<DetailVal<?>>> map = new HashMap<>();
			
			map.put(WTG.Trig.ECA.ECAType.EVENT, eventsItem);
			map.put(WTG.Trig.ECA.ECAType.CONDITION, conditionsItem);
			map.put(WTG.Trig.ECA.ECAType.ACTION, actionsItem);
			
			map.get(subECA.getType()).getChildren().add(addECA(subECA));
		}
		
		if (eventsItem.getChildren().size() > 0) ecaItem.getChildren().add(eventsItem);
		if (conditionsItem.getChildren().size() > 0) ecaItem.getChildren().add(conditionsItem);
		if (actionsItem.getChildren().size() > 0) ecaItem.getChildren().add(actionsItem);
		
		return ecaItem;
	}

	private void setDetail(WTG.Trig trig, WCT.Trig wctTrig) {
		_trigTreeView.setRoot(null);
		
		if (trig == null) return;

		TreeItem<DetailVal<?>> root = new TreeItem<>(null);

		_trigTreeView.setRoot(root);
		_trigTreeView.setShowRoot(false);

		if (trig.getType() == WTG.Trig.TrigType.NORMAL) {
			if (trig.isCustomTxt()) {
				TreeItem<DetailVal<?>> txtItem = new TreeItem<>(new DetailVal<>(wctTrig.getText()));
				
				root.getChildren().add(txtItem);
			} else {			
				TreeItem<DetailVal<?>> eventsItem = new TreeItem<>(new DetailVal<>("events"));
				TreeItem<DetailVal<?>> conditionsItem = new TreeItem<>(new DetailVal<>("conditions"));
				TreeItem<DetailVal<?>> actionsItem = new TreeItem<>(new DetailVal<>("actions"));
				
				eventsItem.setGraphic(new ImageView(fetchIcon(new File("ReplaceableTextures\\WorldEditUI\\Editor-TriggerEvent.blp"))));
				conditionsItem.setGraphic(new ImageView(fetchIcon(new File("ReplaceableTextures\\WorldEditUI\\Editor-TriggerCondition.blp"))));
				actionsItem.setGraphic(new ImageView(fetchIcon(new File("ReplaceableTextures\\WorldEditUI\\Editor-TriggerAction.blp"))));
				
				eventsItem.setExpanded(true);
				conditionsItem.setExpanded(true);
				actionsItem.setExpanded(true);
				
				Map<WTG.Trig.ECA.ECAType, TreeItem<DetailVal<?>>> map = new HashMap<>();
				
				map.put(WTG.Trig.ECA.ECAType.EVENT, eventsItem);
				map.put(WTG.Trig.ECA.ECAType.CONDITION, conditionsItem);
				map.put(WTG.Trig.ECA.ECAType.ACTION, actionsItem);
				
				for (WTG.Trig.ECA eca : trig.getECAs()) {
					map.get(eca.getType()).getChildren().add(addECA(eca));
				}
	
				if (eventsItem.getChildren().size() > 0) root.getChildren().add(eventsItem);
				if (conditionsItem.getChildren().size() > 0) root.getChildren().add(conditionsItem);
				if (actionsItem.getChildren().size() > 0) root.getChildren().add(actionsItem);
			}
		}

		if (trig.getType() == WTG.Trig.TrigType.COMMENT) {
			DetailVal<?> descriptionVal = new DetailVal<>(trig.getDescription());
			
			root.getChildren().add(new TreeItem<>(descriptionVal));
		}
	}

	public void setDetail(WTG.Var var) {
		_trigTreeView.setRoot(null);

		if (var == null) return;

		TreeItem<DetailVal<?>> root = new TreeItem<>(null);
		
		_trigTreeView.setRoot(root);
		_trigTreeView.setShowRoot(false);
		
		TreeItem<DetailVal<?>> nameItem = new TreeItem<>(new DetailVal<>(String.format("name: %s", var.getName())));
		TreeItem<DetailVal<?>> typeItem = new TreeItem<>(new DetailVal<>(String.format("type: %s", var.getType())));
		TreeItem<DetailVal<?>> arrayItem = new TreeItem<>(new DetailVal<>(String.format("array: %s", var.isArray() ? var.getArraySize() : "no")));
		TreeItem<DetailVal<?>> initValItem = new TreeItem<>(new DetailVal<>(String.format("initVal: %s", var.getInitVal())));
		
		root.getChildren().add(nameItem);
		root.getChildren().add(typeItem);
		root.getChildren().add(arrayItem);
		root.getChildren().add(initValItem);
	}

	private WTG _wtg;
	private WCT _wct;

	private class PageVal<T> {
		private T _val;
		
		public T getVal() {
			return _val;
		}
		
		@Override
		public String toString() {
			return _val.toString();
		}
		
		public PageVal(T val) {
			_val = val;
		}
	}
	
	private void setupPages() {
		Image commentImg = fetchIcon(new File("ReplaceableTextures\\WorldEditUI\\Actions-Comment.blp"));
		
		TreeItem<PageVal<?>> root = new TreeItem<>(null);
		
		_pagesTreeView.setRoot(root);
		_pagesTreeView.setShowRoot(false);
		
		Map<Integer, TreeItem<PageVal<?>>> catsMap = new HashMap<>();
		
		for (WTG.TrigCat cat : _wtg.getTrigCats()) {
			TreeItem<PageVal<?>> catItem = new TreeItem<>(new PageVal<>(cat));
			
			catItem.setGraphic(new ImageView(fetchIcon(new File("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup.blp"))));
			
			catsMap.put(cat.getIndex(), catItem);
			
			root.getChildren().add(catItem);
		}

		for (WTG.Trig trig : _wtg.getTrigs()) {
			TreeItem<PageVal<?>> pageItem = new TreeItem<>(new PageVal<>(trig));
			
			if (trig.getType() == WTG.Trig.TrigType.NORMAL) {
				Image trigImg = null;
				
				if (trig.isEnabled()) {
					if (trig.isInitiallyOn()) {
						trigImg = fetchIcon(new File("ReplaceableTextures\\WorldEditUI\\Editor-Trigger.blp"));
					} else {
						trigImg = fetchIcon(new File("ReplaceableTextures\\WorldEditUI\\Editor-TriggerUnused.blp"));
					}
				} else {
					if (trig.isInitiallyOn()) {
						trigImg = fetchIcon(new File("ReplaceableTextures\\WorldEditUI\\Editor-TriggerDisabled.blp"));
					} else {
						trigImg = fetchIcon(new File("ReplaceableTextures\\WorldEditUI\\Editor-TriggerUnusedDisabled.blp"));
					}
				}

				pageItem.setGraphic(new ImageView(trigImg));
			}
			if (trig.getType() == WTG.Trig.TrigType.COMMENT) {
				pageItem.setGraphic(new ImageView(commentImg));
			}
			
			catsMap.get(trig.getCatIndex()).getChildren().add(pageItem);
		}
		
		_pagesTreeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<PageVal<?>>>() {
			@Override
			public void changed(ObservableValue<? extends TreeItem<PageVal<?>>> obs, TreeItem<PageVal<?>> oldVal, TreeItem<PageVal<?>> newVal) {
				Object val = newVal.getValue().getVal();

				if (val instanceof WTG.Trig) {
					setDetail((WTG.Trig) val, _wct.getTrigs().get(_wtg.getTrigs().indexOf((WTG.Trig) val)));
				} else {
					setDetail(null, null);
				}
			}
		});
	}
	
	private class VarVal<T> {
		private T _val;
		
		public T getVal() {
			return _val;
		}
		
		@Override
		public String toString() {
			return _val.toString();
		}
		
		public VarVal(T val) {
			_val = val;
		}
	}
	
	private void setupVars() {
		Image varImg = fetchIcon(new File("ReplaceableTextures\\WorldEditUI\\Editor-ScriptVariable.blp"));
		
		TreeItem<VarVal<?>> root = new TreeItem<>(null);
		
		_varsTreeView.setRoot(root);
		_varsTreeView.setShowRoot(false);
		
		Map<String, TreeItem<VarVal<?>>> map = new HashMap<>();

		for (WTG.Var var : _wtg.getVars().values()) {
			TreeItem<VarVal<?>> varItem = new TreeItem<>(new VarVal<>(var));
			
			varItem.setGraphic(new ImageView(varImg));
			
			TreeItem<VarVal<?>> typeItem = map.get(var.getType());
			
			if (!map.containsKey(var.getType())) {
				typeItem = new TreeItem<>(new VarVal<>(var.getType()));
				
				map.put(var.getType(), typeItem);
				
				root.getChildren().add(typeItem);
			}
			
			typeItem.getChildren().add(varItem);
		}

		_varsTreeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<VarVal<?>>>() {
			@Override
			public void changed(ObservableValue<? extends TreeItem<VarVal<?>>> obs, TreeItem<VarVal<?>> oldVal, TreeItem<VarVal<?>> newVal) {
				Object val = newVal.getValue().getVal();

				if (val instanceof WTG.Var) {
					setDetail((WTG.Var) val);
				} else {
					setDetail(null, null);
				}
			}
		});
	}*/

	private class AttributeRow {
		private ObjMod.Obj.Field _field;

		public StringProperty _nameProperty = new SimpleStringProperty();
		
		private Map<Integer, StringProperty> _valProperties = new HashMap<>();

		public StringProperty getValProperty(int index) {
			return _valProperties.get(index);
		}
		
		private int _maxLevel = 0;
		
		public int getMaxLevel() {
			return _maxLevel;
		}

		public AttributeRow(ObjMod.Obj.Field field) {
			_field = field;

			_nameProperty.set(String.format("%s", field.getId()));
			
			for (Map.Entry<Integer, ObjMod.Obj.Field.Val> valEntry : field.getVals().entrySet()) {
				int level = valEntry.getKey();
				ObjMod.Obj.Field.Val val = valEntry.getValue();
				
				StringProperty valProperty = new SimpleStringProperty();

				valProperty.set(String.format("%s", _translator.translate(val.toString())));
				
				_valProperties.put(level, valProperty);

				if (level > _maxLevel) {
					_maxLevel = level;
				}
			}
		}
	}

	private void setDetail(ObjMod.Obj obj) {
		_detailTable.getItems().clear();
		_detailTable.getColumns().clear();
		
		TableColumn<AttributeRow, String> nameCol = new TableColumn<>("name");

		_detailTable.getColumns().add(nameCol);
		
		_detailTable.setEditable(false);
		_detailTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		for (TableColumn<AttributeRow, ?> col : _detailTable.getColumns()) {
			if (_detailTable.getColumns().get(_detailTable.getColumns().size() - 1) == col) {
				//col.prefWidthProperty().bind(tableView.widthProperty().multiply(0.34));
			} else {
				col.prefWidthProperty().bind(_detailTable.widthProperty().multiply(0.5));
			}
			
			col.impl_setReorderable(false);
			col.setSortable(false);
		}
		
		nameCol.setCellValueFactory(new Callback<CellDataFeatures<AttributeRow, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<AttributeRow, String> param) {
				return param.getValue()._nameProperty;
			}
		});
		
		List<TableColumn<AttributeRow, String>> valCols = new ArrayList<>();
		
		for (ObjMod.Obj.Field field : obj.getFields().values()) {
			AttributeRow row = new AttributeRow(field);

			while (row.getMaxLevel() > valCols.size() - 1) {
				final int index = valCols.size();

				String valColTitle = String.format("val (%d)", index + 1);
				
				if (index == 0) {
					valColTitle = String.format("val (0/1)");
				}
				
				TableColumn<AttributeRow, String> valCol = new TableColumn<>(valColTitle);
				
				valCol.setCellValueFactory(new Callback<CellDataFeatures<AttributeRow, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<AttributeRow, String> param) {
						return param.getValue().getValProperty(index);
					}
				});
				
				valCols.add(valCol);
				
				_detailTable.getColumns().add(valCol);
			}
			
			_detailTable.getItems().add(row);
		}
	}

	private class ObjRow {
		private ObjMod.Obj _obj;

		public ObjMod.Obj getObj() {
			return _obj;
		}

		@Override
		public String toString() {
			return _obj.toString();
		}
		
		public ObjRow(ObjMod.Obj obj) {
			_obj = obj;
		}
	}
	
	private void setupObjs() {
		Map<TitledPane, ObjMod> objMods = new HashMap<>();
		
		objMods.put(_abilitiesPane, _w3a);
		objMods.put(_buffsPane, _w3h);
		objMods.put(_doodadsPane, _w3d);
		objMods.put(_destructablesPane, _w3b);
		objMods.put(_itemsPane, _w3t);
		objMods.put(_unitsPane, _w3u);
		objMods.put(_upgradesPane, _w3q);

		for (Map.Entry<TitledPane, ObjMod> entry : objMods.entrySet()) {
			TitledPane pane = entry.getKey();
			ObjMod objMod = entry.getValue();
			
			if (objMod == null) {
				pane.setText(String.format("%s (not available)", pane.getText()));
			} else {
				pane.setText(String.format("%s (%d)", pane.getText(), objMod.getObjs().size()));
				
				ListView<ObjRow> list = new ListView<>();
				
				pane.setContent(list);
				
				for (ObjMod.Obj obj : objMod.getObjs().values()) {
					ObjRow row = new ObjRow(obj);
					
					list.getItems().add(row);
				}
		
				list.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ObjRow>() {
					@Override
					public void changed(ObservableValue<? extends ObjRow> obs, ObjRow oldVal, ObjRow newVal) {
						setDetail(newVal.getObj());
					}
				});
			}
		}
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			try {
				_w3a = W3A.ofMapFile(_mapFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				_w3h = W3H.ofMapFile(_mapFile);
			} catch (Exception e) {
			}
			try {
				_w3d = W3D.ofMapFile(_mapFile);
			} catch (Exception e) {
			}
			try {
				_w3b = W3B.ofMapFile(_mapFile);
			} catch (Exception e) {
			}
			try {
				_w3t = W3T.ofMapFile(_mapFile);
			} catch (Exception e) {
			}
			try {
				_w3u = W3U.ofMapFile(_mapFile);
			} catch (Exception e) {
			}
			try {
				_w3q = W3Q.ofMapFile(_mapFile);
			} catch (Exception e) {
			}

			setupObjs();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
