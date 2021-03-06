/*    
Craftbook 
Copyright (C) 2010 Lymia <lymiahugs@gmail.com>

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

package lymia.customic;

import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;

import lymia.plc.PlcBase;
import lymia.plc.PlcException;
import lymia.plc.PlcLang;

class CustomICBase extends PlcBase {
    private final String name, code;
    CustomICBase(PlcLang language, String name, String code) {
        super(language);
        this.name = name;
        this.code = code;
    }

    public String getTitle() {
        return name;
    }
    
    protected String getCode(CraftBookWorld cbworld, Vector v) throws PlcException {
        return code;
    }
    protected String validateEnviromentEx(CraftBookWorld cbworld, Vector v, SignText t) {
    	if(t.getLine4().isEmpty())
    		t.setLine4("AAAAAAAAAAAA");
        return null;
    }
}
