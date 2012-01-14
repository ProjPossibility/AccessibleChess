/*
    DroidFish - An Android chess program.
    Copyright (C) 2011  Peter Österlund, peterosterlund2@gmail.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.petero.droidfish;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;

public class ColorTheme {
    private static ColorTheme inst = null;

    /** Get singleton instance. */
    static final ColorTheme instance() {
        if (inst == null)
            inst = new ColorTheme();
        return inst;
    }

    final static int DARK_SQUARE = 0;
    final static int BRIGHT_SQUARE = 1;
    final static int SELECTED_SQUARE = 2;
    final static int CURSOR_SQUARE = 3;
    final static int DARK_PIECE = 4;
    final static int BRIGHT_PIECE = 5;
    final static int CURRENT_MOVE = 6;
    final static int ARROW_0 = 7;
    final static int ARROW_1 = 8;
    final static int ARROW_2 = 9;
    final static int ARROW_3 = 10;
    final static int ARROW_4 = 11;
    final static int ARROW_5 = 12;
    final static int SQUARE_LABEL = 13;
    private final static int numColors = 14;

    private int colorTable[] = new int[numColors];

    private static final String[] prefNames = {
        "darkSquare", "brightSquare", "selectedSquare", "cursorSquare", "darkPiece", "brightPiece", "currentMove",
        "arrow0", "arrow1", "arrow2", "arrow3", "arrow4", "arrow5", "squareLabel"
    };
    private static final String prefPrefix = "color_";

    private final static int defaultTheme = 2;
    final static String[] themeNames = { "Original", "XBoard", "Blue", "Grey" };
    private final static String themeColors[][] = {
    {
        "#FF808080", "#FFBEBE5A", "#FFFF0000", "#FF00FF00", "#FF000000", "#FFFFFFFF", "#FF888888",
        "#A01F1FFF", "#A0FF1F1F", "#501F1FFF", "#50FF1F1F", "#1E1F1FFF", "#28FF1F1F", "#FFFF0000"
    },
    {
        "#FF77A26D", "#FFC8C365", "#FFFFFF00", "#FF00FF00", "#FF202020", "#FFFFFFCC", "#FF6B9262",
        "#A01F1FFF", "#A0FF1F1F", "#501F1FFF", "#50FF1F1F", "#1E1F1FFF", "#28FF1F1F", "#FFFF0000"
    },
    {
        "#FF83A5D2", "#FFFFFFFA", "#FF3232D1", "#FF5F5FFD", "#FF282828", "#FFF0F0F0", "#FF3333FF",
        "#A01F1FFF", "#A01FFF1F", "#501F1FFF", "#501FFF1F", "#1E1F1FFF", "#281FFF1F", "#FFFF0000"
    },
    {
        "#FF666666", "#FFDDDDDD", "#FFFF0000", "#FF0000FF", "#FF000000", "#FFFFFFFF", "#FF888888",
        "#A01F1FFF", "#A0FF1F1F", "#501F1FFF", "#50FF1F1F", "#1E1F1FFF", "#28FF1F1F", "#FFFF0000"
    }
    };

    final void readColors(SharedPreferences settings) {
        for (int i = 0; i < numColors; i++) {
            String prefName = prefPrefix + prefNames[i];
            String defaultColor = themeColors[defaultTheme][i];
            String colorString = settings.getString(prefName, defaultColor);
            colorTable[i] = 0;
            try {
                colorTable[i] = Color.parseColor(colorString);
            } catch (IllegalArgumentException e) {
            } catch (StringIndexOutOfBoundsException e) {
            }
        }
    }

    final void setTheme(SharedPreferences settings, int themeType) {
        Editor editor = settings.edit();
        for (int i = 0; i < numColors; i++)
            editor.putString(prefPrefix + prefNames[i], themeColors[themeType][i]);
        editor.commit();
        readColors(settings);
    }

    final int getColor(int colorType) {
        return colorTable[colorType];
    }
}
