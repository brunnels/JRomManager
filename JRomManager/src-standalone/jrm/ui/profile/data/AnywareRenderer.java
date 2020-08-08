/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.ui.profile.data;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.locale.Messages;
import jrm.profile.data.Disk;
import jrm.profile.data.Entity;
import jrm.profile.data.EntityBase;
import jrm.profile.data.Rom;
import jrm.profile.data.Sample;
import jrm.ui.MainFrame;

// TODO: Auto-generated Javadoc
/**
 * The Class AnywareRenderer.
 */
@SuppressWarnings("serial")
public final class AnywareRenderer
{
	
	/** The Constant columns. */
	public final static String[] columns = new String[] { Messages.getString("AnywareRenderer.Status"), Messages.getString("AnywareRenderer.Name"), Messages.getString("AnywareRenderer.Size"), Messages.getString("AnywareRenderer.CRC"), Messages.getString("AnywareRenderer.MD5"), Messages.getString("AnywareRenderer.SHA-1"), Messages.getString("AnywareRenderer.Merge"), Messages.getString("AnywareRenderer.DumpStatus") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
	
	/** The Constant columnsTypes. */
	public final static Class<?>[] columnsTypes = new Class<?>[] { Object.class, Object.class, Long.class, String.class, String.class, String.class, String.class, Object.class };
	
	/** The Constant columnsWidths. */
	public final static int[] columnsWidths = new int[] { -3, 256, -12, -10, -34, -42, 100, -3 };
	
	/** The Constant columnsRenderers. */
	public final static TableCellRenderer[] columnsRenderers = new TableCellRenderer[] { new DefaultTableCellRenderer()
	{
		ImageIcon bullet_green = MainFrame.getIcon("/jrm/resicons/icons/bullet_green.png"); //$NON-NLS-1$
		ImageIcon bullet_red = MainFrame.getIcon("/jrm/resicons/icons/bullet_red.png"); //$NON-NLS-1$
		ImageIcon bullet_black = MainFrame.getIcon("/jrm/resicons/icons/bullet_black.png"); //$NON-NLS-1$

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
		{
			setBackground(AnywareRenderer.getBackground(row, column));
			super.getTableCellRendererComponent(table,"", isSelected, hasFocus, row, column);
			if (value != null)
			{
				switch (((EntityBase) value).getStatus())
				{
					case OK:
						setIcon(bullet_green);
						break;
					case KO:
						setIcon(bullet_red);
						break;
					case UNKNOWN:
					default:
						setIcon(bullet_black);
						break;
				}
			}
			return this;
		}
	}, new DefaultTableCellRenderer()
	{
		ImageIcon rom_small = MainFrame.getIcon("/jrm/resicons/rom_small.png"); //$NON-NLS-1$
		ImageIcon drive = MainFrame.getIcon("/jrm/resicons/icons/drive.png"); //$NON-NLS-1$
		ImageIcon sound = MainFrame.getIcon("/jrm/resicons/icons/sound.png"); //$NON-NLS-1$

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
		{
			setBackground(AnywareRenderer.getBackground(row, column));
			super.getTableCellRendererComponent(table, (value != null) ? (value instanceof EntityBase ? ((EntityBase) value).getBaseName() : value.toString()) : null, isSelected, hasFocus, row, column);
			if (value instanceof Rom)
				setIcon(rom_small);
			else if (value instanceof Disk)
				setIcon(drive);
			else if (value instanceof Sample)
				setIcon(sound);
			setToolTipText(getText());
			return this;
		}
	}, new DefaultTableCellRenderer()
	{
		{// anonymous constructor
			setHorizontalAlignment(SwingConstants.TRAILING);
		}

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
		{
			setBackground(AnywareRenderer.getBackground(row, column));
			return super.getTableCellRendererComponent(table, value != null ? (value instanceof Long ? ((Long) value > 0 ? value.toString() : null) : value.toString()) : null, isSelected, hasFocus, row, column);
		}
	}, new DefaultTableCellRenderer()
	{
		{
			setHorizontalAlignment(SwingConstants.TRAILING);
		}

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
		{
			setBackground(AnywareRenderer.getBackground(row, column));
			super.getTableCellRendererComponent(table, value != null ? value.toString() : null, isSelected, hasFocus, row, column);
			setFont(new Font(Font.MONOSPACED, getFont().getStyle(), getFont().getSize()));
			return this;
		}
	}, new DefaultTableCellRenderer()
	{
		{
			setHorizontalAlignment(SwingConstants.TRAILING);
		}

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
		{
			setBackground(AnywareRenderer.getBackground(row, column));
			super.getTableCellRendererComponent(table, value != null ? value.toString() : null, isSelected, hasFocus, row, column);
			setFont(new Font(Font.MONOSPACED, getFont().getStyle(), getFont().getSize()));
			return this;
		}
	}, new DefaultTableCellRenderer()
	{
		{
			setHorizontalAlignment(SwingConstants.TRAILING);
		}

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
		{
			setBackground(AnywareRenderer.getBackground(row, column));
			super.getTableCellRendererComponent(table, value != null ? value.toString() : null, isSelected, hasFocus, row, column);
			setFont(new Font(Font.MONOSPACED, getFont().getStyle(), getFont().getSize()));
			return this;
		}
	}, new DefaultTableCellRenderer()
	{
		{
			setHorizontalAlignment(SwingConstants.LEADING);
		}

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
		{
			setBackground(AnywareRenderer.getBackground(row, column));
			super.getTableCellRendererComponent(table, value != null ? value.toString() : null, isSelected, hasFocus, row, column);
			setToolTipText(getText());
			return this;
		}
	}, new DefaultTableCellRenderer()
	{
		ImageIcon verified = MainFrame.getIcon("/jrm/resicons/icons/star.png"); //$NON-NLS-1$
		ImageIcon good = MainFrame.getIcon("/jrm/resicons/icons/tick.png"); //$NON-NLS-1$
		ImageIcon baddump = MainFrame.getIcon("/jrm/resicons/icons/delete.png"); //$NON-NLS-1$
		ImageIcon nodump = MainFrame.getIcon("/jrm/resicons/icons/error.png"); //$NON-NLS-1$
		{
			setHorizontalAlignment(SwingConstants.CENTER);
		}

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
		{
			setBackground(AnywareRenderer.getBackground(row, column));
			super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
			if (value instanceof Entity.Status)
			{
				switch ((Entity.Status) value)
				{
					case verified:
						setIcon(verified);
						break;
					case good:
						setIcon(good);
						break;
					case baddump:
						setIcon(baddump);
						break;
					case nodump:
						setIcon(nodump);
						break;
				}
			}
			else
				setIcon(null);
			setToolTipText(value != null ? value.toString() : null);
			return this;
		}
	} };

	/**
	 * Gets the background.
	 *
	 * @param row the row
	 * @param col the col
	 * @return the background
	 */
	private static Color getBackground(final int row, final int col)
	{
		if ((col % 2) == 0)
		{
			if ((row % 2) == 0)
				return Color.decode("0xDDDDEE"); //$NON-NLS-1$
			return Color.decode("0xEEEEEE"); //$NON-NLS-1$
		}
		else
		{
			if ((row % 2) == 0)
				return Color.decode("0xEEEEFF"); //$NON-NLS-1$
			return Color.decode("0xFFFFFF"); //$NON-NLS-1$
		}
	}

	/**
	 * Instantiates a new anyware renderer.
	 */
	private AnywareRenderer()
	{
	}

}
