package jrm.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.io.FilenameUtils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import jrm.batch.DirUpdater;
import jrm.batch.TorrentChecker;
import jrm.io.torrent.options.TrntChkMode;
import jrm.locale.Messages;
import jrm.misc.Settings;
import jrm.profile.Profile;
import jrm.profile.scan.options.FormatOptions;
import jrm.profile.scan.options.MergeOptions;
import jrm.ui.basic.JFileDropList;
import jrm.ui.basic.JFileDropMode;
import jrm.ui.basic.JListHintUI;
import jrm.ui.basic.JSDRDropTable;
import jrm.ui.basic.ResultColUpdater;
import jrm.ui.basic.SDRTableModel;
import jrm.ui.basic.SDRTableModel.SrcDstResult;
import jrm.ui.batch.BatchTableModel;
import jrm.ui.progress.Progress;

@SuppressWarnings("serial")
public class BatchToolsPanel extends JPanel
{
	private JFileDropList listBatchToolsDat2DirSrc;
	private JSDRDropTable tableBatchToolsDat2Dir;
	private JSDRDropTable tableBatchToolsTrntChk;
	private JComboBox<TrntChkMode> cbBatchToolsTrntChk;
	private JMenu mnDat2DirPresets;

	/**
	 * Create the panel.
	 */
	public BatchToolsPanel()
	{
		this.setLayout(new BorderLayout(0, 0));

		JTabbedPane batchToolsTabbedPane = new JTabbedPane(JTabbedPane.TOP);
		this.add(batchToolsTabbedPane);

		JPanel panelBatchToolsDat2Dir = new JPanel();
		batchToolsTabbedPane.addTab(Messages.getString("MainFrame.panelBatchToolsDat2Dir.title"), null, panelBatchToolsDat2Dir, null); //$NON-NLS-1$
		GridBagLayout gbl_panelBatchToolsDat2Dir = new GridBagLayout();
		gbl_panelBatchToolsDat2Dir.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panelBatchToolsDat2Dir.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panelBatchToolsDat2Dir.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelBatchToolsDat2Dir.rowWeights = new double[] { 1.0, 1.0, 0.0, Double.MIN_VALUE };
		panelBatchToolsDat2Dir.setLayout(gbl_panelBatchToolsDat2Dir);

		JScrollPane scrollPane_5 = new JScrollPane();
		scrollPane_5.setPreferredSize(new Dimension(2, 200));
		scrollPane_5.setBorder(new TitledBorder(null, Messages.getString("MainFrame.SrcDirs"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		GridBagConstraints gbc_scrollPane_5 = new GridBagConstraints();
		gbc_scrollPane_5.gridwidth = 3;
		gbc_scrollPane_5.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_5.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_5.gridx = 0;
		gbc_scrollPane_5.gridy = 0;
		panelBatchToolsDat2Dir.add(scrollPane_5, gbc_scrollPane_5);

		listBatchToolsDat2DirSrc = new JFileDropList(files -> Settings.setProperty("dat2dir.srcdirs", String.join("|", files.stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList())))); //$NON-NLS-1$ //$NON-NLS-2$
		for (final String s : Settings.getProperty("dat2dir.srcdirs", "").split("\\|")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (!s.isEmpty())
				listBatchToolsDat2DirSrc.getModel().addElement(new File(s));
		listBatchToolsDat2DirSrc.setMode(JFileDropMode.DIRECTORY);
		listBatchToolsDat2DirSrc.setUI(new JListHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		listBatchToolsDat2DirSrc.setToolTipText(Messages.getString("MainFrame.listBatchToolsDat2DirSrc.toolTipText")); //$NON-NLS-1$
		scrollPane_5.setViewportView(listBatchToolsDat2DirSrc);

		JPopupMenu popupMenu_2 = new JPopupMenu();
		MainFrame.addPopup(listBatchToolsDat2DirSrc, popupMenu_2);

		JMenuItem mnDat2DirAddSrcDir = new JMenuItem(Messages.getString("MainFrame.AddSrcDir")); //$NON-NLS-1$
		mnDat2DirAddSrcDir.setEnabled(false);
		popupMenu_2.add(mnDat2DirAddSrcDir);

		JMenuItem mnDat2DirDelSrcDir = new JMenuItem(Messages.getString("MainFrame.DelSrcDir")); //$NON-NLS-1$
		mnDat2DirDelSrcDir.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				listBatchToolsDat2DirSrc.del(listBatchToolsDat2DirSrc.getSelectedValuesList());
			}
		});
		popupMenu_2.add(mnDat2DirDelSrcDir);

		JScrollPane scrollPane_6 = new JScrollPane();
		scrollPane_6.setPreferredSize(new Dimension(2, 200));
		GridBagConstraints gbc_scrollPane_6 = new GridBagConstraints();
		gbc_scrollPane_6.gridwidth = 3;
		gbc_scrollPane_6.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_6.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_6.gridx = 0;
		gbc_scrollPane_6.gridy = 1;
		panelBatchToolsDat2Dir.add(scrollPane_6, gbc_scrollPane_6);

		tableBatchToolsDat2Dir = new JSDRDropTable(new BatchTableModel(), new JSDRDropTable.AddDelCallBack()
		{
			@Override
			public void call(List<SrcDstResult> files)
			{
				JsonArray array = Json.array();
				for (SrcDstResult sdr : files)
				{
					JsonObject jso = Json.object();
					jso.add("src", sdr.src != null ? sdr.src.getAbsolutePath() : null); //$NON-NLS-1$
					jso.add("dst", sdr.dst != null ? sdr.dst.getAbsolutePath() : null); //$NON-NLS-1$
					jso.add("result", sdr.result); //$NON-NLS-1$
					jso.add("selected", sdr.selected); //$NON-NLS-1$
					array.add(jso);
				}
				Settings.setProperty("dat2dir.sdr", array.toString()); //$NON-NLS-1$
			}
		});
		List<SrcDstResult> sdrl = new ArrayList<>();
		for (JsonValue arrv : Json.parse(Settings.getProperty("dat2dir.sdr", "[]")).asArray()) //$NON-NLS-1$ //$NON-NLS-2$
		{
			SrcDstResult sdr = new SrcDstResult();
			JsonObject jso = arrv.asObject();
			JsonValue src = jso.get("src"); //$NON-NLS-1$
			if (src != Json.NULL)
				sdr.src = new File(src.asString());
			JsonValue dst = jso.get("dst"); //$NON-NLS-1$
			if (dst != Json.NULL)
				sdr.dst = new File(dst.asString());
			JsonValue result = jso.get("result"); //$NON-NLS-1$
			sdr.result = result.asString();
			sdr.selected = jso.getBoolean("selected", true); //$NON-NLS-1$
			sdrl.add(sdr);
		}
		tableBatchToolsDat2Dir.getSDRModel().setData(sdrl);
		tableBatchToolsDat2Dir.setCellSelectionEnabled(false);
		tableBatchToolsDat2Dir.setRowSelectionAllowed(true);
		tableBatchToolsDat2Dir.getSDRModel().setSrcFilter(file -> {
			List<String> exts = Arrays.asList("xml", "dat"); //$NON-NLS-1$ //$NON-NLS-2$
			if (file.isFile())
				return exts.contains(FilenameUtils.getExtension(file.getName()));
			else if (file.isDirectory())
				return file.listFiles(f -> f.isFile() && exts.contains(FilenameUtils.getExtension(f.getName()))).length > 0;
			return false;
		});
		tableBatchToolsDat2Dir.getSDRModel().setDstFilter(file -> {
			return file.isDirectory();
		});
		tableBatchToolsDat2Dir.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tableBatchToolsDat2Dir.setFillsViewportHeight(true);
		((BatchTableModel)tableBatchToolsDat2Dir.getModel()).applyColumnsWidths(tableBatchToolsDat2Dir);
		scrollPane_6.setViewportView(tableBatchToolsDat2Dir);

		JPopupMenu popupMenu = new JPopupMenu();
		MainFrame.addPopup(tableBatchToolsDat2Dir, popupMenu);
		popupMenu.addPopupMenuListener(new PopupMenuListener()
		{
			public void popupMenuCanceled(PopupMenuEvent e)
			{
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
			{
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e)
			{
				mnDat2DirPresets.setEnabled(tableBatchToolsDat2Dir.getSelectedRowCount() > 0);
			}
		});

		JMenuItem mnDat2DirAddDat = new JMenuItem(Messages.getString("MainFrame.AddDat")); //$NON-NLS-1$
		mnDat2DirAddDat.setEnabled(false);
		popupMenu.add(mnDat2DirAddDat);

		JMenuItem mnDat2DirDelDat = new JMenuItem(Messages.getString("MainFrame.DelDat")); //$NON-NLS-1$
		mnDat2DirDelDat.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				tableBatchToolsDat2Dir.del(tableBatchToolsDat2Dir.getSelectedValuesList());
			}
		});
		popupMenu.add(mnDat2DirDelDat);

		mnDat2DirPresets = new JMenu(Messages.getString("MainFrame.Presets")); //$NON-NLS-1$
		popupMenu.add(mnDat2DirPresets);

		JMenu mnDat2DirD2D = new JMenu(Messages.getString("MainFrame.Dir2DatMenu")); //$NON-NLS-1$
		mnDat2DirPresets.add(mnDat2DirD2D);

		JMenuItem mntmDat2DirD2DTzip = new JMenuItem(Messages.getString("MainFrame.TZIP")); //$NON-NLS-1$
		mntmDat2DirD2DTzip.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				for (SrcDstResult sdr : tableBatchToolsDat2Dir.getSelectedValuesList())
				{
					Properties settings = new Properties();
					try
					{
						settings.setProperty("need_sha1_or_md5", Boolean.FALSE.toString()); //$NON-NLS-1$
						settings.setProperty("use_parallelism", Boolean.TRUE.toString()); //$NON-NLS-1$
						settings.setProperty("create_mode", Boolean.TRUE.toString()); //$NON-NLS-1$
						settings.setProperty("createfull_mode", Boolean.FALSE.toString()); //$NON-NLS-1$
						settings.setProperty("ignore_unneeded_containers", Boolean.FALSE.toString()); //$NON-NLS-1$
						settings.setProperty("ignore_unneeded_entries", Boolean.FALSE.toString()); //$NON-NLS-1$
						settings.setProperty("ignore_unknown_containers", Boolean.TRUE.toString()); //$NON-NLS-1$
						settings.setProperty("implicit_merge", Boolean.FALSE.toString()); //$NON-NLS-1$
						settings.setProperty("ignore_merge_name_roms", Boolean.FALSE.toString()); //$NON-NLS-1$
						settings.setProperty("ignore_merge_name_disks", Boolean.FALSE.toString()); //$NON-NLS-1$
						settings.setProperty("exclude_games", Boolean.FALSE.toString()); //$NON-NLS-1$
						settings.setProperty("exclude_machines", Boolean.FALSE.toString()); //$NON-NLS-1$
						settings.setProperty("backup", Boolean.TRUE.toString()); //$NON-NLS-1$
						settings.setProperty("format", FormatOptions.TZIP.toString()); //$NON-NLS-1$
						settings.setProperty("merge_mode", MergeOptions.NOMERGE.toString()); //$NON-NLS-1$
						settings.setProperty("archives_and_chd_as_roms", Boolean.FALSE.toString()); //$NON-NLS-1$
						Profile.saveSettings(sdr.src, settings);
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				}
			}
		});
		mnDat2DirD2D.add(mntmDat2DirD2DTzip);

		JMenuItem mntmDat2DirD2DDir = new JMenuItem(Messages.getString("MainFrame.DIR")); //$NON-NLS-1$
		mntmDat2DirD2DDir.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				for (SrcDstResult sdr : tableBatchToolsDat2Dir.getSelectedValuesList())
				{
					Properties settings = new Properties();
					try
					{
						settings.setProperty("need_sha1_or_md5", Boolean.FALSE.toString()); //$NON-NLS-1$
						settings.setProperty("use_parallelism", Boolean.TRUE.toString()); //$NON-NLS-1$
						settings.setProperty("create_mode", Boolean.TRUE.toString()); //$NON-NLS-1$
						settings.setProperty("createfull_mode", Boolean.FALSE.toString()); //$NON-NLS-1$
						settings.setProperty("ignore_unneeded_containers", Boolean.FALSE.toString()); //$NON-NLS-1$
						settings.setProperty("ignore_unneeded_entries", Boolean.FALSE.toString()); //$NON-NLS-1$
						settings.setProperty("ignore_unknown_containers", Boolean.TRUE.toString()); //$NON-NLS-1$
						settings.setProperty("implicit_merge", Boolean.FALSE.toString()); //$NON-NLS-1$
						settings.setProperty("ignore_merge_name_roms", Boolean.FALSE.toString()); //$NON-NLS-1$
						settings.setProperty("ignore_merge_name_disks", Boolean.FALSE.toString()); //$NON-NLS-1$
						settings.setProperty("exclude_games", Boolean.FALSE.toString()); //$NON-NLS-1$
						settings.setProperty("exclude_machines", Boolean.FALSE.toString()); //$NON-NLS-1$
						settings.setProperty("backup", Boolean.TRUE.toString()); //$NON-NLS-1$
						settings.setProperty("format", FormatOptions.DIR.toString()); //$NON-NLS-1$
						settings.setProperty("merge_mode", MergeOptions.NOMERGE.toString()); //$NON-NLS-1$
						settings.setProperty("archives_and_chd_as_roms", Boolean.TRUE.toString()); //$NON-NLS-1$
						Profile.saveSettings(sdr.src, settings);
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				}
			}
		});
		mnDat2DirD2D.add(mntmDat2DirD2DDir);

		
		JCheckBox cbBatchToolsDat2DirDryRun = new JCheckBox(Messages.getString("MainFrame.cbBatchToolsDat2DirDryRun.text")); //$NON-NLS-1$
		cbBatchToolsDat2DirDryRun.setSelected(Settings.getProperty("dat2dir.dry_run", false)); //$NON-NLS-1$
		cbBatchToolsDat2DirDryRun.addItemListener(e -> Settings.setProperty("dat2dir.dry_run", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$

		JButton btnBatchToolsDir2DatStart = new JButton(Messages.getString("MainFrame.btnStart.text")); //$NON-NLS-1$
		btnBatchToolsDir2DatStart.addActionListener((e)->dat2dir(cbBatchToolsDat2DirDryRun.isSelected()));

		GridBagConstraints gbc_cbBatchToolsDat2DirDryRun = new GridBagConstraints();
		gbc_cbBatchToolsDat2DirDryRun.insets = new Insets(0, 0, 0, 5);
		gbc_cbBatchToolsDat2DirDryRun.gridx = 1;
		gbc_cbBatchToolsDat2DirDryRun.gridy = 2;
		panelBatchToolsDat2Dir.add(cbBatchToolsDat2DirDryRun, gbc_cbBatchToolsDat2DirDryRun);
		GridBagConstraints gbc_btnBatchToolsDir2DatStart = new GridBagConstraints();
		gbc_btnBatchToolsDir2DatStart.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnBatchToolsDir2DatStart.gridx = 2;
		gbc_btnBatchToolsDir2DatStart.gridy = 2;
		panelBatchToolsDat2Dir.add(btnBatchToolsDir2DatStart, gbc_btnBatchToolsDir2DatStart);

		JPanel panelBatchToolsDir2Torrent = new JPanel();
		batchToolsTabbedPane.addTab(Messages.getString("MainFrame.panelBatchToolsDir2Torrent.title"), null, panelBatchToolsDir2Torrent, null); //$NON-NLS-1$
		GridBagLayout gbl_panelBatchToolsDir2Torrent = new GridBagLayout();
		gbl_panelBatchToolsDir2Torrent.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panelBatchToolsDir2Torrent.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelBatchToolsDir2Torrent.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelBatchToolsDir2Torrent.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		panelBatchToolsDir2Torrent.setLayout(gbl_panelBatchToolsDir2Torrent);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panelBatchToolsDir2Torrent.add(scrollPane, gbc_scrollPane);

		tableBatchToolsTrntChk = new JSDRDropTable(new BatchTableModel(new String[] { Messages.getString("MainFrame.TorrentFiles"), Messages.getString("MainFrame.DstDirs"), Messages.getString("MainFrame.Result") , "Selected"}), new JSDRDropTable.AddDelCallBack() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{
			@Override
			public void call(List<SrcDstResult> files)
			{
				JsonArray array = Json.array();
				for (SrcDstResult sdr : files)
				{
					JsonObject jso = Json.object();
					jso.add("src", sdr.src != null ? sdr.src.getAbsolutePath() : null); //$NON-NLS-1$
					jso.add("dst", sdr.dst != null ? sdr.dst.getAbsolutePath() : null); //$NON-NLS-1$
					jso.add("result", sdr.result); //$NON-NLS-1$
					jso.add("selected", sdr.selected); //$NON-NLS-1$
					array.add(jso);
				}
				Settings.setProperty("trntchk.sdr", array.toString()); //$NON-NLS-1$
			}
		});
		((BatchTableModel)tableBatchToolsTrntChk.getModel()).applyColumnsWidths(tableBatchToolsTrntChk);
		List<SrcDstResult> sdrl2 = new ArrayList<>();
		for (JsonValue arrv : Json.parse(Settings.getProperty("trntchk.sdr", "[]")).asArray()) //$NON-NLS-1$ //$NON-NLS-2$
		{
			SrcDstResult sdr = new SrcDstResult();
			JsonObject jso = arrv.asObject();
			JsonValue src = jso.get("src"); //$NON-NLS-1$
			if (src != Json.NULL)
				sdr.src = new File(src.asString());
			JsonValue dst = jso.get("dst"); //$NON-NLS-1$
			if (dst != Json.NULL)
				sdr.dst = new File(dst.asString());
			JsonValue result = jso.get("result"); //$NON-NLS-1$
			sdr.result = result.asString();
			sdr.selected = jso.getBoolean("selected", true); //$NON-NLS-1$
			sdrl2.add(sdr);
		}
		tableBatchToolsTrntChk.getSDRModel().setData(sdrl2);
		tableBatchToolsTrntChk.setCellSelectionEnabled(false);
		tableBatchToolsTrntChk.setRowSelectionAllowed(true);
		tableBatchToolsTrntChk.getSDRModel().setSrcFilter(file -> {
			List<String> exts = Arrays.asList("torrent"); //$NON-NLS-1$
			if (file.isFile())
				return exts.contains(FilenameUtils.getExtension(file.getName()));
			return false;
		});
		tableBatchToolsTrntChk.getSDRModel().setDstFilter(file -> {
			return file.isDirectory();
		});
		tableBatchToolsTrntChk.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tableBatchToolsTrntChk.setFillsViewportHeight(true);
		scrollPane.setViewportView(tableBatchToolsTrntChk);
		
		JPopupMenu pmTrntChk = new JPopupMenu();
		MainFrame.addPopup(tableBatchToolsTrntChk, pmTrntChk);
		
		JMenuItem mntmAddTorrent = new JMenuItem(Messages.getString("MainFrame.mntmAddTorrent.text")); //$NON-NLS-1$
		mntmAddTorrent.setEnabled(false);
		pmTrntChk.add(mntmAddTorrent);
		
		JMenuItem mntmDelTorrent = new JMenuItem(Messages.getString("MainFrame.mntmDelTorrent.text")); //$NON-NLS-1$
		mntmDelTorrent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tableBatchToolsTrntChk.del(tableBatchToolsTrntChk.getSelectedValuesList());
			}
		});
		pmTrntChk.add(mntmDelTorrent);

		JLabel lblCheckMode = new JLabel(Messages.getString("MainFrame.lblCheckMode.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblCheckMode = new GridBagConstraints();
		gbc_lblCheckMode.insets = new Insets(0, 0, 0, 5);
		gbc_lblCheckMode.anchor = GridBagConstraints.EAST;
		gbc_lblCheckMode.gridx = 0;
		gbc_lblCheckMode.gridy = 1;
		panelBatchToolsDir2Torrent.add(lblCheckMode, gbc_lblCheckMode);

		cbBatchToolsTrntChk = new JComboBox<>();
		cbBatchToolsTrntChk.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Settings.setProperty("trntchk.mode", cbBatchToolsTrntChk.getSelectedItem().toString()); //$NON-NLS-1$
			}
		});
		cbBatchToolsTrntChk.setModel(new DefaultComboBoxModel<TrntChkMode>(TrntChkMode.values()));
		cbBatchToolsTrntChk.setSelectedItem(TrntChkMode.valueOf(Settings.getProperty("trntchk.mode", TrntChkMode.FILENAME.toString()))); //$NON-NLS-1$
		GridBagConstraints gbc_cbBatchToolsTrntChk = new GridBagConstraints();
		gbc_cbBatchToolsTrntChk.anchor = GridBagConstraints.EAST;
		gbc_cbBatchToolsTrntChk.insets = new Insets(0, 0, 0, 5);
		gbc_cbBatchToolsTrntChk.gridx = 1;
		gbc_cbBatchToolsTrntChk.gridy = 1;
		panelBatchToolsDir2Torrent.add(cbBatchToolsTrntChk, gbc_cbBatchToolsTrntChk);

		JButton btnBatchToolsTrntChkStart = new JButton(Messages.getString("MainFrame.btnStart_1.text")); //$NON-NLS-1$
		btnBatchToolsTrntChkStart.addActionListener((e)->trrntChk());
		GridBagConstraints gbc_btnBatchToolsTrntChkStart = new GridBagConstraints();
		gbc_btnBatchToolsTrntChkStart.anchor = GridBagConstraints.EAST;
		gbc_btnBatchToolsTrntChkStart.gridx = 2;
		gbc_btnBatchToolsTrntChkStart.gridy = 1;
		panelBatchToolsDir2Torrent.add(btnBatchToolsTrntChkStart, gbc_btnBatchToolsTrntChkStart);
	}

	private void dat2dir(boolean dryrun)
	{
		if (listBatchToolsDat2DirSrc.getModel().getSize() > 0)
		{
			List<SrcDstResult> sdrl = ((SDRTableModel) tableBatchToolsDat2Dir.getModel()).getData();
			if (sdrl.stream().filter((sdr) -> !Profile.getSettingsFile(sdr.src).exists()).count() > 0)
				JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), Messages.getString("MainFrame.AllDatsPresetsAssigned")); //$NON-NLS-1$
			else
			{
				final Progress progress = new Progress(SwingUtilities.getWindowAncestor(this));
				final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
				{

					@Override
					protected Void doInBackground() throws Exception
					{
						new DirUpdater(sdrl, progress, Collections.list(listBatchToolsDat2DirSrc.getModel().elements()), tableBatchToolsDat2Dir, dryrun);
						return null;
					}

					@Override
					protected void done()
					{
						progress.dispose();
					}

				};
				worker.execute();
				progress.setVisible(true);
			}
		}
		else
			JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), Messages.getString("MainFrame.AtLeastOneSrcDir")); //$NON-NLS-1$
	}

	private void trrntChk()
	{
		final Progress progress = new Progress(SwingUtilities.getWindowAncestor(this));
		final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{

			@Override
			protected Void doInBackground() throws Exception
			{
				List<SrcDstResult> sdrl = ((SDRTableModel) tableBatchToolsTrntChk.getModel()).getData();
				TrntChkMode mode = (TrntChkMode)cbBatchToolsTrntChk.getSelectedItem();
				ResultColUpdater updater = tableBatchToolsTrntChk;
				new TorrentChecker(progress, sdrl, mode, updater);
				return null;
			}

			@Override
			protected void done()
			{
				progress.dispose();
			}

		};
		worker.execute();
		progress.setVisible(true);
	}



}
