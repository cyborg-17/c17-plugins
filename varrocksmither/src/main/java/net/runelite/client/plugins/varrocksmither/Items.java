/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.varrocksmither;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.widgets.WidgetInfo;

@Getter
@AllArgsConstructor
public enum Items
{
	AXE("Axe", WidgetInfo.SMITHING_ANVIL_AXE),
	DAGGER("Dagger",WidgetInfo.SMITHING_ANVIL_DAGGER),
	SWORD("Sword", WidgetInfo.SMITHING_ANVIL_SWORD),
	SCIMITAR("Scimitar", WidgetInfo.SMITHING_ANVIL_SCIMITAR),
	LONG_SWORD("Long Sword", WidgetInfo.SMITHING_ANVIL_LONG_SWORD),
	TWO_H_SWORD("2H Sword", WidgetInfo.SMITHING_ANVIL_TWO_H_SWORD),
	MACE("Mace", WidgetInfo.SMITHING_ANVIL_MACE),
	WARHAMMER("Warhammer", WidgetInfo.SMITHING_ANVIL_WARHAMMER),
	BATTLE_AXE("Battle Axe", WidgetInfo.SMITHING_ANVIL_BATTLE_AXE),
	CLAWS("Claws",WidgetInfo.SMITHING_ANVIL_CLAWS),
	CHAIN_BODY("Chain Body", WidgetInfo.SMITHING_ANVIL_CHAIN_BODY),
	PLATE_LEGS("Plate Legs", WidgetInfo.SMITHING_ANVIL_PLATE_LEGS),
	PLATE_SKIRT("Plate Skirt", WidgetInfo.SMITHING_ANVIL_PLATE_SKIRT),
	PLATE_BODY("Plate Body", WidgetInfo.SMITHING_ANVIL_PLATE_BODY),
	NAILS("Nails", WidgetInfo.SMITHING_ANVIL_NAILS),
	MED_HELM("Med Helm", WidgetInfo.SMITHING_ANVIL_MED_HELM),
	FULL_HELM("Full Helm", WidgetInfo.SMITHING_ANVIL_FULL_HELM),
	SQ_SHIELD("SQ Shield", WidgetInfo.SMITHING_ANVIL_SQ_SHIELD),
	KITE_SHIELD("Kite Shield", WidgetInfo.SMITHING_ANVIL_KITE_SHIELD),
	DART_TIPS("Dart Tips", WidgetInfo.SMITHING_ANVIL_DART_TIPS),
	ARROW_HEADS("Arrow Heads", WidgetInfo.SMITHING_ANVIL_ARROW_HEADS),
	KNIVES("Knives", WidgetInfo.SMITHING_ANVIL_KNIVES),
	JAVELIN_HEADS("Javelin Heads", WidgetInfo.SMITHING_ANVIL_JAVELIN_HEADS),
	BOLTS("Bolts", WidgetInfo.SMITHING_ANVIL_BOLTS),
	LIMBS("Limbs", WidgetInfo.SMITHING_ANVIL_LIMBS);
	//EXCLUSIVE1("WidgetID.SMITHING_GROUP_ID", WidgetID.Smithing.EXCLUSIVE1),
	//EXCLUSIVE2("WidgetID.SMITHING_GROUP_ID", WidgetID.Smithing.EXCLUSIVE2),

	private String name;
	private WidgetInfo item;

	@Override
	public String toString()
	{
		return getName();
	}
}
