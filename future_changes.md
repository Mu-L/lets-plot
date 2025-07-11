## [4.7.0] - 2025-07-dd

### Added

- Geometries:

    - `geom_sina()` [[#1298](https://github.com/JetBrains/lets-plot/issues/1298)].

      See: [example notebook](https://nbviewer.org/github/JetBrains/lets-plot/blob/master/docs/f-25b/geom_sina.ipynb).
  
    - `geom_text_repel()` and `geom_label_repel()` for avoiding text overlaps in plots [[#1092](https://github.com/JetBrains/lets-plot/issues/1092)].  
      See: [example notebook](https://nbviewer.org/github/JetBrains/lets-plot/blob/master/docs/f-25b/ggrepel.ipynb).

- Combining Discrete and Continuous Layers [[#1279](https://github.com/JetBrains/lets-plot/issues/1279)].  
  See: [example notebook](https://nbviewer.org/github/JetBrains/lets-plot/blob/master/docs/f-25b/numeric_data_on_discrete_scale.ipynb).
- `waterfall_plot` - extra layers support [[#1344](https://github.com/JetBrains/lets-plot/issues/1344)].  
  See: [example notebook](https://nbviewer.org/github/JetBrains/lets-plot/blob/master/docs/f-25b/waterfall_plot_layers.ipynb).
- `geom_crossbar()` - annotation support  
  See: [example notebook](https://nbviewer.org/github/JetBrains/lets-plot/blob/master/docs/f-25b/geom_crossbar_annotation.ipynb).
- `waterfall_plot` - now uses `geom_crossbar()` layer annotations to render labels. Added support for `relative_labels` and `absolute_labels` parameters.  
  See: [example notebook](https://nbviewer.org/github/JetBrains/lets-plot/blob/master/docs/f-25b/waterfall_plot_annotations.ipynb).
- More variants to specify a color by name:
    - all HTML/CSS colors;
    - different naming styles like `dark-gray`, `darkgrey`, `dark_grey`, `DARKGRAY`, etc.;
    - grayscale colors from `gray0` (black) to `gray100` (white);
    - See [the complete list](https://lets-plot.org/python/pages/named_colors.html)

- Time Series Plotting [[#278](https://github.com/JetBrains/lets-plot-kotlin/issues/278)], 
[[discussion](https://github.com/JetBrains/lets-plot-kotlin/discussions/92#discussioncomment-12976040)],
[[#678](https://github.com/JetBrains/lets-plot/issues/678)],
[[LPK-129](https://github.com/JetBrains/lets-plot-kotlin/issues/129)]:
  - Support for Python `time` and `date` objects.
  - Support for timezone-aware `datetime` objects and Pandas/Polars `Series`.
  - New `axis_text_spacing`, `axis_text_spacing_x`, and `axis_text_spacing_y` parameters in `theme()` to control spacing between axis ticks and labels.

### Changed

- [**BREAKING**] The `position_dodgev()` function and the `'dodgev'` value for the `position` parameter are deprecated and will be removed in future releases.
- [**BREAKING**] The y-oriented boxplot now use the aesthetics `xlower`/`xmiddle`/`xupper` instead of  `lower`/`middle`/`upper`.
- Updated RGB values for `lightgray` and `green`. To restore the previous colors, use `gray75` and `lime`, respectively. 
- `geom_violin`: tooltips are not shown in the centerline of the violin if `show_half != 0`.
- `geom_crossbar`: the midline is not shown in the legend when `fatten` is set to 0, or when there is no mapping for it.
- `waterfall_plot`: the appearance of the legend has been improved.
- `geom_pointrange`: the midpoint will not be drawn if the y aesthetic is set to `None`.
- `geom_band`: the `alpha` aesthetic only affects the inner part of the geometry, as in `geom_rect()`.
- `geom_band`: show tooltip over the whole band, not just at the edges.
- `ggsave`: the `w` and `h` parameters override plot size, allowing to specify the output image size independently of the plot size.
- `ggsave`: the `dpi` default value changed to 300.
- `ggsave`: the `unit` default value changed to `in` (inches).
- Plot layout: reduced margins and spacing for title, caption, axes, and legend.

### Fixed

- AWT: plot prevents wheel events from bubbling up to the parent component.
- Add tooltip for `geom_hline` and `geom_vline` on `geom_livemap` [[#1056](https://github.com/JetBrains/lets-plot/issues/1056)].
- `geom_boxplot`: unable to draw a y-oriented plot with `stat='identity'` [[#1319](https://github.com/JetBrains/lets-plot/issues/1319)].
- Can't add layer which uses continuous data to a plot where other layers use discrete input [[#1323](https://github.com/JetBrains/lets-plot/issues/1323)].
- Multiline legend labels are not vertically centered with their keys [[#1331](https://github.com/JetBrains/lets-plot/issues/1331)].   
- Poor alignment in legend between columns [[#1332](https://github.com/JetBrains/lets-plot/issues/1332)].
- Ordered data gets re-ordered by geomBoxplot [[#1342](https://github.com/JetBrains/lets-plot/issues/1342)].
- `geom_rect`: fix data conversion for `geom_livemap` [[#1347](https://github.com/JetBrains/lets-plot/issues/1347)].
- ggsave: incorrect output when exporting markdown demo to PNG [[#1362](https://github.com/JetBrains/lets-plot/issues/1362)].
