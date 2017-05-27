package core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import net.moonlightflower.wc3libs.bin.app.WCT;
import net.moonlightflower.wc3libs.bin.app.WTG;
import net.moonlightflower.wc3libs.bin.app.WTG.FuncCat;
import net.moonlightflower.wc3libs.bin.app.WTG.Trig.ECA;
import net.moonlightflower.wc3libs.misc.FieldId;
import net.moonlightflower.wc3libs.misc.UnsupportedFormatException;
import net.moonlightflower.wc3libs.misc.image.Wc3RasterImg;
import net.moonlightflower.wc3libs.port.MpqPort;
import net.moonlightflower.wc3libs.port.Orient;

public class TrigsWindow implements Initializable {
	@FXML
	private TreeView<VarVal<?>> _varsTreeView;
	@FXML
	private TreeView<PageVal<?>> _pagesTreeView;
	@FXML
	private TreeView<DetailVal<?>> _trigTreeView;
	
	private File _mapFile;
	
	private Stage _stage;
	
	public TrigsWindow(File mapFile) throws IOException {
		_mapFile = mapFile;
		
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/core/TrigsWindow.fxml"));
		
		fxmlLoader.setController(this);

		Parent root = fxmlLoader.load();

		Scene scene = new Scene(root);
		
		_stage = new Stage();
		
		_stage.setScene(scene);
		_stage.setTitle("wc3mapStats - Triggers");
	}
	
	public void show() {
		_stage.show();
		_stage.requestFocus();
	}

	private class DetailVal<T> {
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
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			_wtg = WTG.ofMapFile(_mapFile);
			_wct = WCT.ofMapFile(_mapFile);
			
			setupVars();
			setupPages();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
