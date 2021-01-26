package dev.emi.trinkets.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface Trinket {

	/**
	 * Called every tick on the client and server side
	 */
	default void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
	}

	/**
	 * Called when an entity equips a trinket
	 *
	 * @param stack The stack being equipped
	 */
	default void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
	}

	/**
	 * Called when an entity equips a trinket
	 *
	 * @param stack The stack being unequipped
	 */
	default void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
	}

	default boolean canEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
		return true;
	}

	default boolean canUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
		return !EnchantmentHelper.hasBindingCurse(stack);
	}

	/**
	 * Returns the Entity Attribute Modifiers for a stack in a slot. Child implementations should
	 * remain pure
	 * @param uuid The UUID to use for creating attributes
	 */
	default Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack,
			SlotReference slot, LivingEntity entity, UUID uuid) {
		Multimap<EntityAttribute, EntityAttributeModifier> map = HashMultimap.create();
		if (stack.hasTag() && stack.getTag().contains("TrinketAttributeModifiers", 9)) {
			ListTag list = stack.getTag().getList("TrinketAttributeModifiers", 10);

			for (int i = 0; i < list.size(); ++i) {
				CompoundTag tag = list.getCompound(i);
				// TODO Determine and setup a slot naming scheme for tags, probably just group:slot, and uncomment the second condition
				if (!tag.contains("Slot", 8)/* || tag.getString("Slot").equals(equipmentSlot.getName())*/) {
					Optional<EntityAttribute> optional = Registry.ATTRIBUTE
							.getOrEmpty(Identifier.tryParse(tag.getString("AttributeName")));
					if (optional.isPresent()) {
						EntityAttributeModifier entityAttributeModifier = EntityAttributeModifier.fromTag(tag);
						if (entityAttributeModifier != null
								&& entityAttributeModifier.getId().getLeastSignificantBits() != 0L
								&& entityAttributeModifier.getId().getMostSignificantBits() != 0L) {
							map.put(optional.get(), entityAttributeModifier);
						}
					}
				}
			}
		}
		return map;
	}

	default void onBreak(ItemStack stack, SlotReference slot, LivingEntity entity) {
		// TODO
	}

	default TrinketEnums.DropRule getDropRule(ItemStack stack, SlotReference slot, LivingEntity entity) {
		return TrinketEnums.DropRule.DEFAULT;
	}

	class SlotReference {
		public SlotType slot;
		public int index;

		public SlotReference(SlotType slot, int index) {
			this.slot = slot;
			this.index = index;
		}
	}
}