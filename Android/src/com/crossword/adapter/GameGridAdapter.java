/*
 * Copyright 2011 Alexis Lauper <alexis.lauper@gmail.com>
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.crossword.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.crossword.R;
import com.crossword.activity.GameActivity;
import com.crossword.data.Word;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class GameGridAdapter extends BaseAdapter {

	public static final int 			AREA_BLOCK = -1;
	public static final int 			AREA_WRITABLE = 0;
	private HashMap<Integer, TextView>	views = new HashMap<Integer, TextView>();
	private Context						context;
	private String[][]					area;			// Tableau représentant les lettres du joueur
	private String[][] 					correctionArea; // Tableau représentant les lettres correctes
	private boolean						isLower;
	private boolean 					isDraft;
	private int 						displayHeight;
	private int 						width;
	private int 						height;

	public GameGridAdapter(Activity act, ArrayList<Word> entries, int width, int height) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(act);
		this.isLower = preferences.getBoolean("grid_is_lower", false);
		this.context = (Context)act;
		this.width = width;
		this.height = height;
		
		// Calcul area height
        Display display = act.getWindowManager().getDefaultDisplay();
        this.displayHeight = display.getWidth() / this.width;

        // Fill area and areaCorrection
        this.area = new String[this.height][this.width];
        this.correctionArea = new String[this.height][this.width];
	    for (Word entry: entries) {
	    	String tmp = entry.getTmp();
	    	String text = entry.getText();
	    	boolean horizontal = entry.getHorizontal();
	    	int x = entry.getX();
	    	int y = entry.getY();
	    	
	    	for (int i = 0 ; i < entry.getLength(); i++) {
	    		if (horizontal)
	    		{
	    			if (y >= 0 && y < this.height && x+i >= 0 && x+i < this.width)
	    			{
	    				this.area[y][x+i] = tmp != null ? String.valueOf(tmp.charAt(i)) : " ";
	    				this.correctionArea[y][x+i] = String.valueOf(text.charAt(i));
	    			}
	    		}
	    		else
	    		{
	    			if (y+i >= 0 && y+i < this.height && x >= 0 && x < this.width)
	    			{
	    				this.area[y+i][x] = tmp != null ? String.valueOf(tmp.charAt(i)) : " ";
	    				this.correctionArea[y+i][x] = String.valueOf(text.charAt(i));
	    			}
	    		}
	    	}
	    }
	    

	    
	}
	
	@Override
	public int getCount() {
		return this.height * this.width;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
//		// Stop le traitement si la vue vient d'etre genere
//		if (this.lastPosition == position)
//			return this.views.get(position);
//		this.lastPosition = position;
		
		TextView v = this.views.get(position);
		int y = (int)(position / this.width);
		int x = (int)(position % this.width);
		String data = this.area[y][x];
		String correction = this.correctionArea[y][x];
		
		// Creation du composant
		if (v == null)
		{
			v = new TextView(context);
			v.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.FILL_PARENT, this.displayHeight));
			v.setTextSize((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4 ? 30 : 20);
			v.setGravity(Gravity.CENTER);

			if (data != null) {
				v.setBackgroundResource(R.drawable.area_empty);
				v.setTag(AREA_WRITABLE);
			} else {
				v.setBackgroundResource(R.drawable.area_block);
				v.setTag(AREA_BLOCK);
			}
			
			this.views.put(position, v);
		}

		// Si la grille est en mode check, colore les fautes en rouge
    	if (((GameActivity)context).currentMode == GameActivity.GRID_MODE.CHECK)
    	{
    		if (data != null) {
    			v.setTextColor(context.getResources().getColor(data.equalsIgnoreCase(correction) ? R.color.normal : R.color.wrong));
    	    	v.setText(this.isLower ? data.toLowerCase() : data.toUpperCase());
    		}
    	}
		// Si la grille est en mode correction, ajoute les bonnes lettres en verte
    	else if (((GameActivity)this.context).currentMode == GameActivity.GRID_MODE.SOLVE)
    	{
    		if (data != null && data.equalsIgnoreCase(correction)) {
    			v.setTextColor(context.getResources().getColor(R.color.normal));
    	    	v.setText(this.isLower ? data.toLowerCase() : data.toUpperCase());
    		} else if (correction != null) {
    			v.setTextColor(context.getResources().getColor(R.color.right));
    	    	v.setText(this.isLower ? correction.toLowerCase() : correction.toUpperCase());
    		}
    	}
    	// Sinon mode normal, text en noire
    	else
    	{
    		if (data != null) {
    			if (Character.isLowerCase(data.charAt(0)))
    				v.setTextColor(context.getResources().getColor(R.color.draft));
    			else
    				v.setTextColor(context.getResources().getColor(R.color.normal));
    			v.setText(this.isLower ? data.toLowerCase() : data.toUpperCase());
    		}
    	}
		
		return v;
	}

	public void setLower(boolean isLower) {
		this.isLower = isLower;
	}

	public int getPercent() {
		int filled = 0;
		int empty = 0;
		
		for (int y = 0; y < this.height; y++)
			for (int x = 0; x < this.width; x++)
				if (this.area[y][x] != null) {
					if (this.area[y][x].equals(" "))
						empty++;
					else
						filled++;
				}
		return filled * 100 / (empty + filled);
	}

	public boolean isBlock(int x, int y) {
		return (this.area[y][x] == null);
	}

	public void setValue(int x, int y, String value) {
		if (this.area[y][x] != null)
			this.area[y][x] = this.isDraft ? value.toLowerCase() : value.toUpperCase();
	}

	public String getWord(int x, int y, int length, boolean isHorizontal) {
    	StringBuffer word = new StringBuffer();
    	for (int i = 0; i < length; i++) {
    		if (isHorizontal) {
    			if (y < this.height && x+i < this.width)
    				word.append(this.area[y][x+i] != null ? this.area[y][x+i] : " ");
    		}
    		else {
    			if (y+i < this.height && x < this.width)
    				word.append(this.area[y+i][x] != null ? this.area[y+i][x] : " ");
    		}
    	}
    	return word.toString();
	}

	public void setDraft(boolean value) {
		this.isDraft = value;
	}

}
