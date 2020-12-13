package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolType;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiClickable;
import se.mickelus.mgui.gui.GuiStringOutline;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.util.CastOptional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CraftButtonGui extends GuiClickable {
    private final GuiStringOutline label;
    private final GuiTexture backdrop;

    private boolean enabled = true;
    private String tooltip;

    private int labelColor = GuiColors.normal;
    private int backdropColor = GuiColors.normal;

    public CraftButtonGui(int x, int y, Runnable onClickHandler) {
        super(x, y, 46, 15, onClickHandler);

        backdrop = new GuiTexture(0, 0, width, height, 176, 16, GuiTextures.workbench);
        backdrop.setAttachment(GuiAttachment.middleCenter);
        addChild(backdrop);

        label = new GuiStringOutline(0, 1, I18n.format("tetra.workbench.schematic_detail.craft"));
        label.setAttachment(GuiAttachment.middleCenter);
        addChild(label);

    }

    @Override
    public boolean onMouseClick(int x, int y, int button) {
        return enabled && super.onMouseClick(x, y, button);
    }

    public void update(UpgradeSchematic schematic, PlayerEntity player, ItemStack itemStack, ItemStack[] materials, String slot,
            Map<ToolType, Integer> availableTools) {
        enabled = schematic.canApplyUpgrade(player, itemStack, materials, slot, availableTools);
        tooltip = null;

        if (enabled) {
            labelColor = GuiColors.normal;
            backdropColor = GuiColors.normal;

            if (!schematic.willReplace(itemStack, materials, slot)) {
                boolean canDestabilize = CastOptional.cast(itemStack.getItem(), ModularItem.class)
                        .map(item -> item.getModuleFromSlot(itemStack, slot))
                        .map(module -> module.getMagicCapacity(itemStack))
                        .map(cap -> cap < 0)
                        .orElse(false);

                if (canDestabilize) {
                    backdropColor = GuiColors.destabilized;
                    tooltip = I18n.format("tetra.workbench.schematic_detail.destabilize_tooltip");
                }
            } else {
                boolean willRepair = CastOptional.cast(itemStack.getItem(), ModularItem.class)
                        .map(item -> item.getRepairSlot(itemStack))
                        .map(repairSlot -> repairSlot.equals(slot))
                        .orElse(false);

                if (willRepair) {
                    tooltip = I18n.format("tetra.workbench.schematic_detail.repair_tooltip");
                }
            }

        } else {
            labelColor = GuiColors.muted;
            backdropColor = GuiColors.negative;

            tooltip = "";
            if (!schematic.isMaterialsValid(itemStack, slot, materials)) {
                if (hasEmptyMaterial(schematic, materials)) {
                    tooltip += I18n.format("tetra.workbench.schematic_detail.no_material_tooltip");
                    backdropColor = GuiColors.muted;
                } else if (hasInsufficientQuantities(schematic, itemStack, slot, materials)) {
                    tooltip += I18n.format("tetra.workbench.schematic_detail.material_count_tooltip");
                } else {
                    tooltip += I18n.format("tetra.workbench.schematic_detail.material_tooltip");
                }
            } else {
                if (schematic.isIntegrityViolation(player, itemStack, materials, slot)) {
                    tooltip += I18n.format("tetra.workbench.schematic_detail.integrity_tooltip");
                }
                if (!schematic.checkTools(itemStack, materials, availableTools)) {
                    tooltip += I18n.format("tetra.workbench.schematic_detail.tools_tooltip");
                }
                if (!player.isCreative() && player.experienceLevel < schematic.getExperienceCost(itemStack, materials, slot)) {
                    tooltip += I18n.format("tetra.workbench.schematic_detail.level_tooltip");
                }
            }
        }

        updateColors();
    }

    private boolean hasEmptyMaterial(UpgradeSchematic schematic, ItemStack[] materials) {
        for (int i = 0; i < schematic.getNumMaterialSlots(); i++) {
            if (materials[i].isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private boolean hasInsufficientQuantities(UpgradeSchematic schematic, ItemStack itemStack, String slot, ItemStack[] materials) {
        for (int i = 0; i < schematic.getNumMaterialSlots(); i++) {
            if (schematic.acceptsMaterial(itemStack, slot, i, materials[i])) {
                int requiredCount = schematic.getRequiredQuantity(itemStack, i, materials[i]);
                if (!materials[i].isEmpty() && requiredCount > materials[i].getCount()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void onFocus() {
        updateColors();
    }

    @Override
    protected void onBlur() {
        updateColors();
    }

    private void updateColors() {
        if (enabled && hasFocus()) {
            label.setColor(GuiColors.hover);
            backdrop.setColor(GuiColors.hover);
        } else {
            label.setColor(labelColor);
            backdrop.setColor(backdropColor);
        }
    }

    @Override
    public List<String> getTooltipLines() {
        if (tooltip != null && hasFocus()) {
            return Collections.singletonList(tooltip);
        }
        return null;
    }
}