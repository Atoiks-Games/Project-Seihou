/**
 *  Nappou-1
 *  Copyright (C) 2017-2018  Atoiks-Games <atoiks-games@outlook.com>

 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.atoiks.games.nappou1;

import java.util.Arrays;

import org.atoiks.games.framework2d.IGraphics;

/**
 *
 * @author YTENG
 */
public class BulletManager {

    // packed order: x, y, r, dx, dy
    private int size;
    private float[] packed;

    private int x1;
    private int y1;
    private int x2;
    private int y2;

    public BulletManager(int cap, int x1, int y1, int x2, int y2) {
        this.size = 0;
//        this.packed = new float[cap * 25];
        this.packed = new float[cap * 5];

        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public BulletManager(int cap) {
        this(cap, 0, 0, 0, 0);
    }

    public void ensureCapacity(int cap) {
        final int tcap = cap * 5;
        if (tcap > this.packed.length) {
            this.packed = Arrays.copyOf(packed, tcap);
        }
    }

    public void trimToSize() {
        this.packed = Arrays.copyOf(packed, size);
    }

    public void addBullet(float x, float y, float r, float dx, float dy) {
        if (this.size >= this.packed.length) {
            this.packed = Arrays.copyOf(packed, packed.length + 5 * 16);
        }

        this.packed[size + 0] = x;
        this.packed[size + 1] = y;
        this.packed[size + 2] = r;
        this.packed[size + 3] = dx;
        this.packed[size + 4] = dy;
        size += 5;
    }

    public void clear() {
        Arrays.fill(this.packed, 0, this.size, 0);
        this.size = 0;
    }

    public void changeBox(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public boolean testCollision(float x, float y, float r) {
        for (int i = 0; i < size; i += 5) {
            if (Utils.circlesCollide(packed[i], packed[i + 1], packed[i + 2],
                    x, y, r)) {
                size -= 5;
                return true;
            }
        }
        return false;
    }

    public void updatePosition(final float dt) {
        int shiftIndex = 0;
        for (int i = 0; i < size; i += 5) {
            final float radius = packed[i + 2];
            final float dx = packed[i + 3];
            final float dy = packed[i + 4];
            final float newX = packed[i] + dx * dt;
            if (newX + radius > x1 && newX - radius < x2) {
                final float newY = packed[i + 1] + dy * dt;
                if (newY + radius > y1 && newY - radius < y2) {
                    packed[shiftIndex + 0] = newX;
                    packed[shiftIndex + 1] = newY;
                    packed[shiftIndex + 2] = radius;
                    packed[shiftIndex + 3] = dx;
                    packed[shiftIndex + 4] = dy;
                    shiftIndex += 5;
                }
            }

            // Reaching here means bullet went out of bounds
        }
        // drop
        size = shiftIndex;
    }

    public void render(IGraphics g) {
        try {
            for (int i = 0; i < size; i += 5) {
                final float radius = packed[i + 2];
                g.drawCircle((int) packed[i], (int) packed[i + 1], (int) radius);
            }
        } catch (IndexOutOfBoundsException | NullPointerException ex) {
        }
    }

    public void firePatternRadial(float originX, float originY,
            float spacing, float tilt, float upperBound,
            float size, float speed) {
        // o -> apply [radial]
        //
        // \ | /
        // - o -   (angle between is spacing (rad), angle offset is tilt (rad))
        // / | \

        if (size == 0) {
            return;
        }

        for (float counter = 0; counter < upperBound; counter += spacing) {
            final float actAngle = counter + tilt;
            addBullet(originX, originY, size,
                    (float) Math.cos(actAngle) * speed,
                    (float) Math.sin(actAngle) * speed);
        }
    }
}
