package com.lc.common.utils.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author l5990
 */
public class TreeNote<T> implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    public static String ROOT = "_ROOT";

    private TreeNote<T> parent;
    private T data;
    private List<TreeNote<T>> subNotes;

    public TreeNote<T> getParent() {
        return parent;
    }

    public void setParent(TreeNote<T> parent) {
        this.parent = parent;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<TreeNote<T>> getSubNotes() {
        return subNotes;
    }

    public void setSubNotes(List<TreeNote<T>> subNotes) {
        this.subNotes = subNotes;
    }

    public void addSubTreeNote(TreeNote<T> subNote) {
        subNote.parent = this;
        if (this.subNotes == null) {
            this.subNotes = new ArrayList<TreeNote<T>>();
        }
        this.subNotes.add(subNote);
    }

    public TreeNote<T> getRoot() {
        TreeNote<T> p = this;
        while (p.parent != null) {
            p = p.parent;
        }
        return p;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TreeNote [");
        if (this.subNotes == null) {
            sb.append(" subNotes = null");
        } else {
            sb.append(" subNotes.size() = ").append(subNotes.size()).append("[\n");
            for (TreeNote<T> tn : subNotes) {
                sb.append(tn.toString());
            }
            sb.append("\n]");
        }
        sb.append("\n]");
        return sb.toString();

    }

}
