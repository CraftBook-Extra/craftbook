// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.craftbook;

import java.util.Map;
import java.util.HashMap;

/**
 * List of block IDs.
 * 
 * @author sk89q
 */
public final class BlockType {    
    public static final int AIR = 0;
    public static final int STONE = 1;
    public static final int GRASS = 2;
    public static final int DIRT = 3;
    public static final int COBBLESTONE = 4;
    public static final int WOOD = 5;
    public static final int SAPLING = 6;
    public static final int BEDROCK = 7;
    public static final int WATER = 8;
    public static final int STATIONARY_WATER = 9;
    public static final int LAVA = 10;
    public static final int STATIONARY_LAVA = 11;
    public static final int SAND = 12;
    public static final int GRAVEL = 13;
    public static final int GOLD_ORE = 14;
    public static final int IRON_ORE = 15;
    public static final int COAL_ORE = 16;
    public static final int LOG = 17;
    public static final int LEAVES = 18;
    public static final int SPONGE = 19;
    public static final int GLASS = 20;
    public static final int LAPIS_LAZULI_ORE = 21;
    public static final int LAPIS_LAZULI = 22;
    public static final int DISPENSER = 23;
    public static final int SANDSTONE = 24;
    public static final int NOTE_BLOCK = 25;
    public static final int BED = 26;
    public static final int POWERED_RAIL = 27;
    public static final int DETECTOR_RAIL = 28;
    public static final int STICKY_PISTON = 29;
    public static final int WEB = 30;
    public static final int TALL_GRASS = 31;
    public static final int DEAD_SHRUBS = 32;
    public static final int PISTON = 33;
    public static final int PISTON_EXTENSION = 34;
    public static final int CLOTH = 35;
    public static final int PISTON_MOVED_BLOCK = 36;
    public static final int YELLOW_FLOWER = 37;
    public static final int RED_FLOWER = 38;
    public static final int BROWN_MUSHROOM = 39;
    public static final int RED_MUSHROOM = 40;
    public static final int GOLD_BLOCK = 41;
    public static final int IRON_BLOCK = 42;
    public static final int DOUBLE_STEP = 43;
    public static final int STEP = 44;
    public static final int BRICK = 45;
    public static final int TNT = 46;
    public static final int BOOKCASE = 47;
    public static final int MOSSY_COBBLESTONE = 48;
    public static final int OBSIDIAN = 49;
    public static final int TORCH = 50;
    public static final int FIRE = 51;
    public static final int MOB_SPAWNER = 52;
    public static final int WOODEN_STAIRS = 53;
    public static final int CHEST = 54;
    public static final int REDSTONE_WIRE = 55;
    public static final int DIAMOND_ORE = 56;
    public static final int DIAMOND_BLOCK = 57;
    public static final int WORKBENCH = 58;
    public static final int CROPS = 59;
    public static final int SOIL = 60;
    public static final int FURNACE = 61;
    public static final int BURNING_FURNACE = 62;
    public static final int SIGN_POST = 63;
    public static final int WOODEN_DOOR = 64;
    public static final int LADDER = 65;
    public static final int MINECART_TRACKS = 66;
    public static final int COBBLESTONE_STAIRS = 67;
    public static final int WALL_SIGN = 68;
    public static final int LEVER = 69;
    public static final int STONE_PRESSURE_PLATE = 70;
    public static final int IRON_DOOR = 71;
    public static final int WOODEN_PRESSURE_PLATE = 72;
    public static final int REDSTONE_ORE = 73;
    public static final int GLOWING_REDSTONE_ORE = 74;
    public static final int REDSTONE_TORCH_OFF = 75;
    public static final int REDSTONE_TORCH_ON = 76;
    public static final int STONE_BUTTON = 77;
    public static final int SNOW = 78;
    public static final int ICE = 79;
    public static final int SNOW_BLOCK = 80;
    public static final int CACTUS = 81;
    public static final int CLAY = 82;
    public static final int REED = 83;
    public static final int JUKEBOX = 84;
    public static final int FENCE = 85;
    public static final int PUMPKIN = 86;
    public static final int NETHERSTONE = 87;
    public static final int SLOW_SAND = 88;
    public static final int LIGHTSTONE = 89;
    public static final int PORTAL = 90;
    public static final int JACKOLANTERN = 91;
    public static final int CAKE_BLOCK = 92;
    public static final int REDSTONE_REPEATER_OFF = 93;
    public static final int REDSTONE_REPEATER_ON = 94;
    public static final int TRAPDOOR = 96;
    public static final int SILVERFISH_BLOCK = 97;
    public static final int STONE_BRICKS = 98;
    public static final int HUGE_BROWN_MUSHROOM = 99;
    public static final int HUGE_RED_MUSHROOM = 100;
    public static final int IRON_BARS = 101;
    public static final int GLASS_PANE = 102;
    public static final int MELON = 103;
    public static final int PUMPKIN_STEM = 104;
    public static final int MELON_STEM = 105;
    public static final int VINES = 106;
    public static final int FENCE_GATE = 107;
    public static final int BRICK_STAIRS = 108;
    public static final int STONE_BRICK_STAIRS = 109;
    public static final int MYCELIUM = 110;
    public static final int LILY_PAD = 111;
    public static final int NETHER_BRICK = 112;
    public static final int NETHER_BRICK_FENCE = 113;
    public static final int NETHER_BRICK_STAIRS = 114;
    public static final int NETHER_WART = 115;
    public static final int ENCHANTMENT_TABLE = 116;
    public static final int BREWING_STAND = 117;
    public static final int CAULDRON = 118;
    public static final int END_PORTAL = 119;
    public static final int END_PORTAL_FRAME = 120;
    public static final int END_STONE = 121;
    public static final int DRAGON_EGG = 122;
    public static final int REDSTONE_LAMP_OFF = 123;
    public static final int REDSTONE_LAMP_ON = 124;
    public static final int WOODEN_DOUBLE_SLAB = 125;
    public static final int WOODEN_SLAB = 126;
    public static final int COCOA_POD = 127;
    public static final int SANDSTONE_STAIRS = 128;
    public static final int EMERALD_ORE = 129;
    public static final int ENDER_CHEST = 130;
    public static final int TRIPWIRE_HOOK = 131;
    public static final int TRIPWIRE = 132;
    public static final int BLOCK_OF_EMERALD = 133;
    public static final int SPRUCE_WOOD_STAIRS = 134;
    public static final int BIRCH_WOOD_STAIRS = 135;
    public static final int JUNGLE_WOOD_STAIRS = 136;
    public static final int COMMAND_BLOCK = 137;
    public static final int BEACON = 138;
    public static final int COBBLESTONE_WALL = 139;
    public static final int FLOWER_POT = 140;
    public static final int CARROT = 141;
    public static final int POTATOES = 142;
    public static final int WOODEN_BUTTON = 143;
    public static final int MOB_HEAD = 144;

    /**
     * Stores a list of dropped blocks for blocks.
     */
    private static final Map<Integer,Integer> blockDrops = new HashMap<Integer,Integer>();

    /**
     * Static constructor.
     */
    static {
        blockDrops.put(1, 4);
        blockDrops.put(2, 3);
        blockDrops.put(3, 3);
        blockDrops.put(4, 4);
        blockDrops.put(5, 5);
        blockDrops.put(6, 6);
        blockDrops.put(7, -1);
        blockDrops.put(12, 12);
        blockDrops.put(13, 13);
        blockDrops.put(14, 14);
        blockDrops.put(15, 15);
        blockDrops.put(16, 16);
        blockDrops.put(17, 17);
        blockDrops.put(18, 18);
        blockDrops.put(19, 19);
        blockDrops.put(20, 20);
        blockDrops.put(21, 351); //default type. needs color value with this, but no other block currently acts like this
        blockDrops.put(22, 22);
        blockDrops.put(23, 23);
        blockDrops.put(24, 24);
        blockDrops.put(25, 25);
        blockDrops.put(26, 355);
        blockDrops.put(27, 27);
        blockDrops.put(28, 28);
        blockDrops.put(29, 29);
        blockDrops.put(30, 30);
        blockDrops.put(31, 31);
        blockDrops.put(32, 32);
        blockDrops.put(33, 33);
        blockDrops.put(34, 34);
        blockDrops.put(35, 35);
        blockDrops.put(36, 36);
        blockDrops.put(37, 37);
        blockDrops.put(38, 38);
        blockDrops.put(39, 39);
        blockDrops.put(40, 40);
        blockDrops.put(41, 41);
        blockDrops.put(42, 42);
        blockDrops.put(43, 43);
        blockDrops.put(44, 44);
        blockDrops.put(45, 45);
        blockDrops.put(47, 47);
        blockDrops.put(48, 48);
        blockDrops.put(49, 49);
        blockDrops.put(50, 50);
        blockDrops.put(53, 53);
        blockDrops.put(54, 54);
        blockDrops.put(55, 331);
        blockDrops.put(56, 56);
        blockDrops.put(57, 57);
        blockDrops.put(58, 58);
        blockDrops.put(59, 295);
        blockDrops.put(60, 60);
        blockDrops.put(61, 61);
        blockDrops.put(62, 61);
        blockDrops.put(63, 323);
        blockDrops.put(64, 324);
        blockDrops.put(65, 65);
        blockDrops.put(66, 66);
        blockDrops.put(67, 67);
        blockDrops.put(68, 323);
        blockDrops.put(69, 69);
        blockDrops.put(70, 70);
        blockDrops.put(71, 330);
        blockDrops.put(72, 72);
        blockDrops.put(73, 331);
        blockDrops.put(74, 331);
        blockDrops.put(75, 76);
        blockDrops.put(76, 76);
        blockDrops.put(77, 77);
        blockDrops.put(80, 80);
        blockDrops.put(81, 81);
        blockDrops.put(82, 82);
        blockDrops.put(83, 83);
        blockDrops.put(84, 84);
        blockDrops.put(85, 85);
        blockDrops.put(86, 86);
        blockDrops.put(87, 87);
        blockDrops.put(88, 88);
        blockDrops.put(89, 89);
        blockDrops.put(91, 91);
        blockDrops.put(92, 354);
        blockDrops.put(93, 356);
        blockDrops.put(94, 356);
        blockDrops.put(96, 96);
        blockDrops.put(97, 97);
        blockDrops.put(98, 98);
        blockDrops.put(99, 99);
        blockDrops.put(100, 100);
        blockDrops.put(101, 101);
        blockDrops.put(102, 102);
        blockDrops.put(103, 103);
        blockDrops.put(104, 104);
        blockDrops.put(105, 105);
        blockDrops.put(106, 106);
        blockDrops.put(107, 107);
        blockDrops.put(108, 108);
        blockDrops.put(109, 109);
        blockDrops.put(110, 110);
        blockDrops.put(111, 111);
        blockDrops.put(112, 112);
        blockDrops.put(113, 113);
        blockDrops.put(114, 114);
        blockDrops.put(115, 115);
        blockDrops.put(116, 116);
        blockDrops.put(117, 117);
        blockDrops.put(118, 118);
        blockDrops.put(119, -1);
        blockDrops.put(120, -1);
        blockDrops.put(121, 121);
        blockDrops.put(122, 122);
        blockDrops.put(123, 123);
        blockDrops.put(124, 123);
        blockDrops.put(125, 125);
        blockDrops.put(126, 126);
        blockDrops.put(127, 127);
        blockDrops.put(128, 128);
        blockDrops.put(129, 388);
        blockDrops.put(130, 130);
        blockDrops.put(131, 131);
        blockDrops.put(132, 287);
        blockDrops.put(133, 133);
        blockDrops.put(134, 134);
        blockDrops.put(135, 135);
        blockDrops.put(136, 136);
        blockDrops.put(137, 137);
        blockDrops.put(138, 138);
        blockDrops.put(139, 139);
        blockDrops.put(140, 140);
        blockDrops.put(141, 391);
        blockDrops.put(142, 392);
        blockDrops.put(143, 143);
        blockDrops.put(144, 144);
    }

    /**
     * Returns true if the block type requires a block underneath.
     * 
     * @param id
     * @return
     */
    public static boolean isBottomDependentBlock(int id) {
        return id == SAPLING
                || id == YELLOW_FLOWER
                || id == RED_FLOWER
                || id == BROWN_MUSHROOM
                || id == RED_MUSHROOM
                || id == TALL_GRASS
                || id == DEAD_SHRUBS
                || id == LILY_PAD
                || id == NETHER_WART
                || id == TORCH
                || id == REDSTONE_WIRE
                || id == CROPS
                || id == CARROT
                || id == POTATOES
                || id == SIGN_POST
                || id == WALL_SIGN
                || id == MINECART_TRACKS
                || id == POWERED_RAIL
                || id == DETECTOR_RAIL
                || id == LEVER
                || id == STONE_PRESSURE_PLATE
                || id == WOODEN_PRESSURE_PLATE
                || id == REDSTONE_TORCH_OFF
                || id == REDSTONE_TORCH_ON
                || id == REDSTONE_REPEATER_OFF
                || id == REDSTONE_REPEATER_ON
                || id == STONE_BUTTON;
    }

    /**
     * Checks to see whether a block should be placed last.
     * 
     * @param id
     * @return
     */
    public static boolean shouldPlaceLast(int id) {
        return id == SAPLING
                || id == YELLOW_FLOWER
                || id == RED_FLOWER
                || id == BROWN_MUSHROOM
                || id == RED_MUSHROOM
                || id == TALL_GRASS
                || id == DEAD_SHRUBS
        		|| id == LILY_PAD
                || id == NETHER_WART
                || id == TORCH
                || id == FIRE
                || id == REDSTONE_WIRE
                || id == CROPS
        		|| id == CARROT
                || id == POTATOES
                || id == COCOA_POD
                || id == SIGN_POST
                || id == WOODEN_DOOR
                || id == TRAPDOOR
                || id == LADDER
                || id == MINECART_TRACKS
                || id == POWERED_RAIL
                || id == DETECTOR_RAIL
                || id == WALL_SIGN
                || id == LEVER
                || id == STONE_PRESSURE_PLATE
                || id == IRON_DOOR
                || id == WOODEN_PRESSURE_PLATE
                || id == REDSTONE_TORCH_OFF
                || id == REDSTONE_TORCH_ON
                || id == REDSTONE_REPEATER_OFF
                || id == REDSTONE_REPEATER_ON
                || id == BED
                || id == STONE_BUTTON
                || id == WOODEN_BUTTON
                || id == TRIPWIRE_HOOK
                || id == SNOW
                || id == CACTUS
                || id == REED
                || id == PORTAL
                || id == FENCE_GATE;
    }

    /**
     * Checks whether a block can be passed through.
     * 
     * @param id
     * @return
     */
    public static boolean canPassThrough(int id) {
        return id == AIR
                || id == SAPLING
                || id == YELLOW_FLOWER
                || id == RED_FLOWER
                || id == BROWN_MUSHROOM
                || id == RED_MUSHROOM
                || id == TALL_GRASS
                || id == DEAD_SHRUBS
                || id == NETHER_WART
                || id == TORCH
                || id == FIRE
                || id == REDSTONE_WIRE
                || id == CROPS
        		|| id == CARROT
                || id == POTATOES
                || id == SIGN_POST
                || id == LADDER
                || id == MINECART_TRACKS
                || id == POWERED_RAIL
                || id == DETECTOR_RAIL
                || id == WALL_SIGN
                || id == LEVER
                || id == STONE_PRESSURE_PLATE
                || id == WOODEN_PRESSURE_PLATE
                || id == REDSTONE_TORCH_OFF
                || id == REDSTONE_TORCH_ON
                || id == REDSTONE_REPEATER_OFF
                || id == REDSTONE_REPEATER_ON
                || id == STONE_BUTTON
                || id == WOODEN_BUTTON
                || id == TRIPWIRE_HOOK
                || id == TRIPWIRE
                || id == SNOW
                || id == REED
                || id == WEB
                || id == PORTAL
                || id == END_PORTAL
                || id == PUMPKIN_STEM
                || id == MELON_STEM
                || id == VINES;
    }

    /**
     * Returns true if the block uses its data value.
     * 
     * @param id
     * @return
     */
    public static boolean usesData(int id) {
        return id == SAPLING
                || id == WATER
                || id == STATIONARY_WATER
                || id == LAVA
                || id == STATIONARY_LAVA
                || id == CHEST
                || id == ENDER_CHEST
                || id == TORCH
                || id == WOODEN_STAIRS
                || id == REDSTONE_WIRE
                || id == CROPS
        		|| id == CARROT
                || id == POTATOES
                || id == COCOA_POD
                || id == SOIL
                || id == TALL_GRASS
                || id == SIGN_POST
                || id == WOODEN_DOOR
                || id == LADDER
                || id == MINECART_TRACKS
                || id == POWERED_RAIL
                || id == DETECTOR_RAIL
                || id == COBBLESTONE_STAIRS
                || id == BRICK_STAIRS
                || id == STONE_BRICK_STAIRS
                || id == NETHER_BRICK_STAIRS
                || id == SANDSTONE_STAIRS
                || id == SPRUCE_WOOD_STAIRS
                || id == BIRCH_WOOD_STAIRS
                || id == JUNGLE_WOOD_STAIRS
                || id == WALL_SIGN
                || id == LEVER
                || id == STONE_PRESSURE_PLATE
                || id == IRON_DOOR
                || id == TRAPDOOR
                || id == WOODEN_PRESSURE_PLATE
                || id == REDSTONE_TORCH_OFF
                || id == REDSTONE_TORCH_ON
                || id == REDSTONE_REPEATER_OFF
                || id == REDSTONE_REPEATER_ON
                || id == STICKY_PISTON
                || id == PISTON
                || id == BED
                || id == STONE_BUTTON
                || id == WOODEN_BUTTON
                || id == TRIPWIRE_HOOK
                || id == TRIPWIRE
                || id == CLOTH
                || id == LOG
                || id == DOUBLE_STEP
                || id == WOODEN_DOUBLE_SLAB
                || id == WOODEN_SLAB
                || id == STEP
                || id == CACTUS
                || id == SILVERFISH_BLOCK
                || id == STONE_BRICKS
                || id == HUGE_BROWN_MUSHROOM
                || id == HUGE_RED_MUSHROOM
        		|| id == PUMPKIN_STEM
                || id == MELON_STEM
                || id == VINES
                || id == NETHER_WART
                || id == FENCE_GATE
                || id == COBBLESTONE_WALL
                || id == BREWING_STAND
                || id == CAULDRON
                || id == FLOWER_POT
                || id == MOB_HEAD
                || id == END_PORTAL_FRAME;
    }

    /**
     * Returns true if an ID is lava.
     * 
     * @param id
     * @return
     */
    public static boolean isLava(int id) {
        return id == STATIONARY_LAVA
                || id == LAVA;
    }
    
    /**
     * Returns true if an ID is water.
     * 
     * @param id
     * @return
     */
    public static boolean isWater(int id) {
        return id == STATIONARY_WATER
                || id == WATER;
    }

    /**
     * Returns true if a block uses redstone in some way.
     *
     * @param id
     * @return
     */
    public static boolean isRedstoneBlock(int id) {
        return id == LEVER
                || id == STONE_PRESSURE_PLATE
                || id == WOODEN_PRESSURE_PLATE
                || id == REDSTONE_TORCH_ON
                || id == REDSTONE_TORCH_OFF
                || id == REDSTONE_REPEATER_OFF
                || id == REDSTONE_REPEATER_ON
        		|| id == REDSTONE_LAMP_ON
                || id == REDSTONE_LAMP_OFF
                || id == STONE_BUTTON
                || id == REDSTONE_WIRE
                || id == STICKY_PISTON
                || id == PISTON
                || id == WOODEN_DOOR
                || id == IRON_DOOR
                || id == TRIPWIRE_HOOK
                || id == COMMAND_BLOCK
                || id == TRAPDOOR;
    }
    
    /**
     * Returns true if a block uses data for color.
     *
     * @param id
     * @return
     */
    public static boolean isColorTypeBlock(int id) {
        return id == CLOTH
                || id == DOUBLE_STEP
                || id == STEP
                || id == LOG
                || id == SAPLING
                || id == TALL_GRASS
                || id == WOODEN_DOUBLE_SLAB
                || id == WOODEN_SLAB
                || id == STONE_BRICKS;
    }
    
    /**
     * Returns true if a block uses data for direction.
     *
     * @param id
     * @return
     */
    public static boolean isDirectionBlock(int id) {
        return id == DISPENSER
        		|| id == BED
                || id == POWERED_RAIL
                || id == DETECTOR_RAIL
                || id == STICKY_PISTON
                || id == PISTON
                || id == TORCH
                || id == WOODEN_STAIRS
                || id == CHEST
                || id == ENDER_CHEST
                || id == FURNACE
                || id == BURNING_FURNACE
                || id == SIGN_POST
                || id == LADDER
                || id == MINECART_TRACKS
                || id == COBBLESTONE_STAIRS
                || id == WALL_SIGN
                || id == LEVER
                || id == IRON_DOOR
        		|| id == REDSTONE_TORCH_ON
                || id == REDSTONE_TORCH_OFF
                || id == STONE_BUTTON
                || id == WOODEN_BUTTON
                || id == PUMPKIN
                || id == JACKOLANTERN
                || id == COCOA_POD
                || id == REDSTONE_REPEATER_OFF
                || id == REDSTONE_REPEATER_ON
                || id == TRAPDOOR
                || id == TRIPWIRE_HOOK
        		|| id == FENCE_GATE
                || id == BRICK_STAIRS
                || id == STONE_BRICK_STAIRS
        		|| id == NETHER_BRICK_STAIRS
        		|| id == SANDSTONE_STAIRS
        		|| id == SPRUCE_WOOD_STAIRS
        		|| id == BIRCH_WOOD_STAIRS
        		|| id == JUNGLE_WOOD_STAIRS
                || id == BREWING_STAND
                ;
    }

    /**
     * Get the block or item that would have been dropped. If nothing is
     * dropped, 0 will be returned. If the block should not be destroyed
     * (i.e. bedrock), -1 will be returned.
     * 
     * @param id
     * @return
     */
    public static int getDroppedBlock(int id) {
        Integer dropped = blockDrops.get(id);
        if (dropped == null) {
            return 0;
        }
        return dropped;
    }
}
