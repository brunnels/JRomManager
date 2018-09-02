package jrm.batch;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.binary.Hex;

import jrm.io.torrent.Torrent;
import jrm.io.torrent.TorrentFile;
import jrm.io.torrent.TorrentParser;
import jrm.io.torrent.options.TrntChkMode;
import jrm.locale.Messages;
import jrm.misc.HTMLRenderer;
import jrm.misc.UnitRenderer;
import jrm.ui.basic.ResultColUpdater;
import jrm.ui.basic.SDRTableModel;
import jrm.ui.basic.SrcDstResult;
import jrm.ui.progress.Progress;
import one.util.streamex.StreamEx;

public class TorrentChecker implements UnitRenderer,HTMLRenderer
{
	AtomicInteger processing = new AtomicInteger();
	AtomicInteger current = new AtomicInteger();

	/**
	 * Check a dir versus torrent data 
	 * @param progress the progressions handler
	 * @param sdrl the data obtained from {@link SDRTableModel}
	 * @param mode the check mode (see {@link TrntChkMode}
	 * @param updater the result interface
	 * @throws IOException
	 */
	public TorrentChecker(final Progress progress, List<SrcDstResult> sdrl, TrntChkMode mode, ResultColUpdater updater, boolean removeUnknownFiles, boolean removeWrongSizedFiles) throws IOException
	{
		progress.setInfos(Math.min(Runtime.getRuntime().availableProcessors(),(int)sdrl.stream().filter(sdr->sdr.selected).count()), true);
		progress.setProgress2("", 0, 1); //$NON-NLS-1$
		StreamEx.of(sdrl).filter(sdr->sdr.selected).forEach(sdr->{
			updater.updateResult(sdrl.indexOf(sdr), "");
		});
		StreamEx.of(sdrl).filter(sdr->sdr.selected).parallel().takeWhile(sdr->!progress.isCancel()).forEach(sdr->{
			try
			{
				int row = sdrl.indexOf(sdr);
				updater.updateResult(row, "In progress...");
				final String result = check(progress, mode, sdr, removeUnknownFiles, removeWrongSizedFiles);
				updater.updateResult(row, result);
				progress.setProgress(null, -1, null, "");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		});
	}

	/**
	 * @param progress
	 * @param mode
	 * @param sdr
	 * @return
	 * @throws IOException
	 */
	private String check(final Progress progress, TrntChkMode mode, SrcDstResult sdr, boolean removeUnknownFiles, boolean removeWrongSizedFiles) throws IOException
	{
		String result = ""; //$NON-NLS-1$
		if (sdr.src != null && sdr.dst != null)
		{
			if (sdr.src.exists() && sdr.dst.exists())
			{
				Torrent torrent = TorrentParser.parseTorrent(sdr.src.getAbsolutePath());
				List<TorrentFile> tfiles = torrent.getFileList();
				int total = tfiles.size(), ok = 0;
				long missing_bytes = 0;
				int missing_files = 0;
				int wrong_sized_files = 0;
				HashSet<Path> paths = new HashSet<>();
				if (mode != TrntChkMode.SHA1)
				{
					processing.addAndGet(total);
					for (int j = 0; j < total; j++)
					{
						current.incrementAndGet();
						TorrentFile tfile = tfiles.get(j);
						Path file = sdr.dst.toPath();
						for (String path : tfile.getFileDirs())
							file = file.resolve(path);
						paths.add(file.toAbsolutePath());
						progress.setProgress(toHTML(toPurple(sdr.src.getAbsolutePath())), -1, null, file.toString());
						progress.setProgress2(current + "/" + processing, current.get(), processing.get()); //$NON-NLS-1$
						if (Files.exists(file))
						{
							if(mode == TrntChkMode.FILENAME || Files.size(file) == tfile.getFileLength())
								ok++;
							else
							{
								if(removeWrongSizedFiles)
									Files.delete(file);
								wrong_sized_files++;
								missing_bytes += tfile.getFileLength();
							}
						}
						else
						{
							if(mode == TrntChkMode.FILENAME)
								missing_files++;
							else
								missing_bytes += tfile.getFileLength();
						}
						if(progress.isCancel())
							break;
					}
					int removed_files = removeUnknownFiles(paths, sdr, removeUnknownFiles && !progress.isCancel());
					if(ok == total)
					{
						if(removed_files>0)
							result = toHTML(toBold(toBlue(Messages.getString("TorrentChecker.ResultComplete"))));
						else
							result = toHTML(toBold(toGreen(Messages.getString("TorrentChecker.ResultComplete"))));
					}
					else if(mode == TrntChkMode.FILENAME)
						result = String.format(Messages.getString("TorrentChecker.ResultFileName"), ok * 100.0 / total, missing_files, removed_files); //$NON-NLS-1$
					else
						result = String.format(Messages.getString("TorrentChecker.ResultFileSize"), ok * 100.0 / total, humanReadableByteCount(missing_bytes, false), wrong_sized_files, removed_files); //$NON-NLS-1$
				}
				else
				{
					try
					{
						long piece_length = torrent.getPieceLength();
						List<String> pieces = torrent.getPieces();
						long to_go = piece_length;
						int piece_cnt = 0, piece_valid = 0;
						processing.addAndGet(pieces.size());
						progress.setProgress(sdr.src.getAbsolutePath(), -1, null, ""); //$NON-NLS-1$
						progress.setProgress2(String.format(Messages.getString("TorrentChecker.PieceProgression"), current.get(), processing.get()), -1, processing.get()); //$NON-NLS-1$
						piece_cnt++;
						boolean valid = true;
						MessageDigest md = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
						byte[] buffer = new byte[8192];
						for (TorrentFile tfile : tfiles)
						{
							BufferedInputStream in = null;
							Path file = sdr.dst.toPath();
							for (String path : tfile.getFileDirs())
								file = file.resolve(path);
							paths.add(file.toAbsolutePath());
							if (!Files.exists(file))
								valid = false;
							else if(Files.size(file) != tfile.getFileLength())
							{
								if(removeWrongSizedFiles)
									Files.delete(file);
								wrong_sized_files++;
								valid = false;
							}
							else
								in = new BufferedInputStream(new FileInputStream(file.toFile()));
							progress.setProgress(toHTML(toPurple(sdr.src.getAbsolutePath())), -1, null, file.toString());
							long flen = tfile.getFileLength();
							while (flen >= to_go)
							{
								if (in != null)
								{
									long to_read = to_go;
									do
									{
										int len = in.read(buffer, 0, (int) (to_read < buffer.length ? to_read : buffer.length));
										md.update(buffer, 0, len);
										to_read -= len;
									}
									while (to_read > 0);
								}
								flen -= to_go;
								to_go = (int) piece_length;
								progress.setProgress2(String.format(Messages.getString("TorrentChecker.PieceProgression"), current.get(), processing.get()), current.get(), processing.get()); //$NON-NLS-1$
								if (valid && Hex.encodeHexString(md.digest()).equalsIgnoreCase(pieces.get(piece_cnt - 1)))
									piece_valid++;
								else
									missing_bytes += piece_length;
								md.reset();
								piece_cnt++;
								current.incrementAndGet();
								valid = true;
								if (flen > 0)
								{
									if (!Files.exists(file) || Files.size(file) != tfile.getFileLength())
										valid = false;
								}
							}
							if (in != null)
							{
								long to_read = flen;
								do
								{
									int len = in.read(buffer, 0, (int) (to_read < buffer.length ? to_read : buffer.length));
									md.update(buffer, 0, len);
									to_read -= len;
								}
								while (to_read > 0);
								in.close();
							}
							to_go -= flen;
							if(progress.isCancel())
								break;
						}
						progress.setProgress2(String.format(Messages.getString("TorrentChecker.PieceProgression"), current.get(), processing.get()), current.get(), processing.get()); //$NON-NLS-1$
						if (valid && Hex.encodeHexString(md.digest()).equalsIgnoreCase(pieces.get(piece_cnt - 1)))
							piece_valid++;
						else
							missing_bytes += piece_length - to_go;
						System.out.format("piece counted %d, given %d, valid %d, completion=%.02f%%\n", piece_cnt, pieces.size(), piece_valid, piece_valid * 100.0 / piece_cnt); //$NON-NLS-1$
						System.out.format("piece len : %d\n", piece_length); //$NON-NLS-1$
						System.out.format("last piece len : %d\n", piece_length - to_go); //$NON-NLS-1$
						int removed_files = removeUnknownFiles(paths, sdr, removeUnknownFiles && !progress.isCancel());
						if(piece_valid == piece_cnt)
						{
							if(removed_files>0)
								result = toHTML(toBold(toBlue(Messages.getString("TorrentChecker.ResultComplete"))));
							else
								result = toHTML(toBold(toGreen(Messages.getString("TorrentChecker.ResultComplete"))));
						}
						else
							result = String.format(Messages.getString("TorrentChecker.ResultSHA1"), piece_valid * 100.0 / piece_cnt, humanReadableByteCount(missing_bytes, false), wrong_sized_files, removed_files); //$NON-NLS-1$
					}
					catch (Exception ex)
					{
						result = ex.getMessage();
					}
				}
			}
			else
				result = sdr.src.exists() ? Messages.getString("TorrentChecker.DstMustExist") : Messages.getString("TorrentChecker.SrcMustExist"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
			result = sdr.src == null ? Messages.getString("TorrentChecker.SrcNotDefined") : Messages.getString("TorrentChecker.DstNotDefined"); //$NON-NLS-1$ //$NON-NLS-2$
		return result;
	}
	
	private int removeUnknownFiles(HashSet<Path> paths, SrcDstResult sdr, boolean remove) throws IOException
	{
		List<Path> files_to_remove = new ArrayList<>();
		Files.walkFileTree(sdr.dst.toPath(), new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
			{
				if (!paths.contains(file.toAbsolutePath()))
					files_to_remove.add(file);
				return super.visitFile(file, attrs);
			}
		});
		int count = files_to_remove.size();
		if (remove)
		{
			files_to_remove.forEach(t -> {
				try
				{
					Files.delete(t);
				}
				catch (IOException e)
				{
				}
			});
		}
		return count;
	}

}