using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Data;

namespace TreasureHuntDesktopApplication.FullClient.Project_Utilities
{
    //-http://wpftutorial.net/ValueConverters.html
    public class BoolToVisibilityConverter : IValueConverter
    {
        //-http://nocodemonkey.blogspot.co.uk/2012/12/wpf-bool-to-visibility-converter.html
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            var boolean = false;
            if (value is bool) boolean = (bool)value;
            return boolean ? Visibility.Visible : Visibility.Hidden;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return value is Visibility && (Visibility)value == Visibility.Visible;
        }
    }
}
