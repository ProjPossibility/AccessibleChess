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

package org.petero.droidfish.engine;

public interface UCIEngine {

    /** Start engine. */
    public void initialize();

    /** Shut down engine. */
    public void shutDown();

    /**
     * Read a line from the engine.
     * @param timeoutMillis Maximum time to wait for data
     * @return The line, without terminating newline characters,
     *         or empty string if no data available,
     *         or null if I/O error.
     */
    public String readLineFromEngine(int timeoutMillis);

    /** Write a line to the engine. \n will be added automatically. */
    public void writeLineToEngine(String data);

    /** Set the engine strength, allowed values 0 - 1000. */
    public void setStrength(int strength);

    /** Add strength information to the engine name. */
    public String addStrengthToName();

    /** Set an engine option. */
    public void setOption(String name, int value);
    public void setOption(String name, boolean value);
    public void setOption(String name, String value);
}
