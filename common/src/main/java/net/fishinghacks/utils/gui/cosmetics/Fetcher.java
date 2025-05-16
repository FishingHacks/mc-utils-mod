package net.fishinghacks.utils.gui.cosmetics;

import java.util.List;

public interface Fetcher {
    int currentPage();
    boolean hasNext();
    boolean hasPrev();
    List<CosmeticsEntry> getFetched();
}
