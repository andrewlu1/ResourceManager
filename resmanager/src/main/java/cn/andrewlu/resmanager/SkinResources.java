package cn.andrewlu.resmanager;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Movie;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;

/**
 * Created by andrewlu on 2018/1/20.
 */

public class SkinResources extends Resources {
    private final Resources superResources;

    public SkinResources(AssetManager assets, Resources superResource) {
        super(assets, superResource.getDisplayMetrics(), superResource.getConfiguration());
        this.superResources = superResource;
    }

    public interface IdMapFilter {
        Integer filter(int reId);
    }

    private IdMapFilter idMapFilter = null;

    public void setFilter(IdMapFilter fileter) {
        idMapFilter = fileter;
    }

    public Integer filterId(int resId) {
        if (idMapFilter != null) {
            return idMapFilter.filter(resId);
        }
        return resId;
    }

    public boolean getBoolean(int resId) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                return super.getBoolean(_id);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getBoolean(_id);
    }

    public int getColor(int resId) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                return super.getColor(_id);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getColor(_id);
    }

    public ColorStateList getColorStateList(int resId) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                return super.getColorStateList(_id);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getColorStateList(_id);
    }

    public float getDimension(int resId) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                return super.getDimension(_id);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getDimension(_id);
    }

    public int getDimensionPixelOffset(int resId) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                return super.getDimensionPixelOffset(_id);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getDimensionPixelOffset(_id);
    }

    public int getDimensionPixelSize(int resId) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                return super.getDimensionPixelSize(_id);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getDimensionPixelSize(_id);
    }

    public Drawable getDrawable(int resId) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                return super.getDrawable(_id);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getDrawable(_id);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    public Drawable getDrawableForDensity(int resId, int density) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                return super.getDrawableForDensity(_id, density);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getDrawableForDensity(_id, density);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Typeface getFont(int resId) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                return super.getFont(_id);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getFont(_id);
    }

    public float getFraction(int resId, int base, int pbase) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                return super.getFraction(_id, base, pbase);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getFraction(_id, base, pbase);
    }

    public int getInteger(int resId) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                return super.getInteger(_id);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getInteger(_id);
    }

    public Movie getMovie(int resId) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                return super.getMovie(_id);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getMovie(_id);
    }

    public String getQuantityString(int resId, int quantity) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                return super.getQuantityString(_id, quantity);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getQuantityString(_id, quantity);
    }

    public String getQuantityString(int resId, int quentity, Object... args) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                return super.getQuantityString(_id, quentity, args);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getQuantityString(_id, quentity, args);
    }

    public String getString(int resId) {
        int _id = idMapFilter != null ? idMapFilter.filter(resId) : resId;
        try {
            return super.getString(_id);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getString(_id);
    }

    public String getString(int resId, Object... args) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                return super.getString(_id, args);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getString(_id, args);
    }

    public String[] getStringArray(int resId) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                return super.getStringArray(_id);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getStringArray(_id);
    }

    public CharSequence getText(int resId) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                return super.getText(_id);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getText(_id);
    }

    public CharSequence[] getTextArray(int resId) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                return super.getTextArray(_id);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getTextArray(_id);
    }

    public CharSequence getText(int resId, CharSequence def) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                return super.getText(_id);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return superResources.getText(_id, def);
    }

    public void getValue(int resId, TypedValue outValue, boolean resoveRefs) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                super.getValue(_id, outValue, resoveRefs);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        superResources.getValue(_id, outValue, resoveRefs);
    }

    public void getValue(String name, TypedValue outValue, boolean resoveRefs) {
        try {
            super.getValue(name, outValue, resoveRefs);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        superResources.getValue(name, outValue, resoveRefs);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    public void getValueForDensity(int resId, int density, TypedValue outValue, boolean resoveRefs) {
        Integer _id = filterId(resId);
        try {
            if (_id != null) {
                super.getValueForDensity(_id, density, outValue, resoveRefs);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        superResources.getValueForDensity(_id, density, outValue, resoveRefs);
    }
}
