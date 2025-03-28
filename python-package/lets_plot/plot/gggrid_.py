#
# Copyright (c) 2023. JetBrains s.r.o.
# Use of this source code is governed by the MIT license that can be found in the LICENSE file.
#

from ._global_theme import _get_global_theme
from .subplots import SupPlotsLayoutSpec
from .subplots import SupPlotsSpec
from .subplots_util import _strip_theme_if_global

__all__ = ['gggrid']


def gggrid(plots: list, ncol: int = None, *,
           sharex: str = None,
           sharey: str = None,
           widths: list = None,
           heights: list = None,
           hspace: float = None,
           vspace: float = None,
           fit: bool = None,
           align: bool = None
           ) -> SupPlotsSpec:
    """
    Combine several plots on one figure, organized in a regular grid.

    Parameters
    ----------
    plots : list
        A list where each element is a plot specification, a subplots specification, or None.
        Use value None to fill-in empty cells in grid.
    ncol : int
        Number of columns in grid.
        If not specified, shows plots horizontally, in one row.
    sharex, sharey : bool or str, default=False
        Controls sharing of axis limits between subplots in the grid.

        - 'all'/True - share limits between all subplots.
        - 'none'/False - do not share limits between subplots.
        - 'row' - share limits between subplots in the same row.
        - 'col' - share limits between subplots in the same column.

    widths : list of numbers
        Relative width of each column of grid, left to right.
    heights : list of numbers
        Relative height of each row of grid, top-down.
    hspace : float, default=4.0
        Cell horizontal spacing in px.
    vspace : float, default=4.0
        Cell vertical spacing in px.
    fit : bool, default=True
        Whether to stretch each plot to match the aspect ratio of its cell (`fit=True`),
        or to preserve the original aspect ratio of plots (`fit=False`).
    align : bool, default=False
        If True, align inner areas (i.e. "geom" bounds) of plots.
        However, cells containing other (sub)grids are not participating in the plot "inner areas" layouting.

    Returns
    -------
    `SupPlotsSpec`
        The grid specification.

    Examples
    --------
    .. jupyter-execute::
        :linenos:
        :emphasize-lines: 11, 14

        import numpy as np
        from lets_plot import *
        LetsPlot.setup_html()
        np.random.seed(42)
        n = 100
        x = np.arange(n)
        y = np.random.normal(size=n)
        w, h = 200, 150
        p = ggplot({'x': x, 'y': y}, aes(x='x', y='y')) + ggsize(w, h)
        plot_list=[
            gggrid([p+geom_point(), p+geom_histogram(bins=3)]),
            p+geom_line()
        ]
        gggrid(plot_list, ncol=1) + ggsize(400, 300)

    """

    if not len(plots):
        raise ValueError("Supplots list is empty.")

    if ncol is None:
        ncol = len(plots)
        nrow = 1
    else:
        extended_list = plots + [None] * (ncol - 1)
        nrow = len(extended_list) // ncol
        length = ncol * nrow
        plots = extended_list[0:length]

    if sharex is not None and type(sharex) != str:
        sharex = 'all' if sharex else 'none'
    if sharey is not None and type(sharey) != str:
        sharey = 'all' if sharey else 'none'

    layout = SupPlotsLayoutSpec(
        name="grid",
        ncol=ncol,
        nrow=nrow,
        sharex=sharex,
        sharey=sharey,
        widths=widths,
        heights=heights,
        hspace=hspace,
        vspace=vspace,
        fit=fit,
        align=align
    )

    figures = [_strip_theme_if_global(fig) for fig in plots]

    figure_spec = SupPlotsSpec(figures=figures, layout=layout)

    # Apply global theme if defined
    global_theme_options = _get_global_theme()
    if global_theme_options is not None:
        figure_spec += global_theme_options

    return figure_spec
