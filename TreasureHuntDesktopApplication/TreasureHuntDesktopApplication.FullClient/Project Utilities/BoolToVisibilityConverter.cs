using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Data;

//----------------------------------------------------------
//<copyright>
//</copyright>
//----------------------------------------------------------

namespace TreasureHuntDesktopApplication.FullClient.Project_Utilities
{
    /// <Summary>  The purpose of this class is to convert between boolean and visibility values which are non compatible. 
    /// Used in the context of deciding whether or not displaying pop up loading screens should be visible or
    /// hidden on screen. </Summary>

    //-http://wpftutorial.net/ValueConverters.html
    public class BoolToVisibilityConverter : IValueConverter
    {
        //-http://nocodemonkey.blogspot.co.uk/2012/12/wpf-bool-to-visibility-converter.html/
        /// <summary>
        ///  Method that converts from boolean to Visibility 
        /// </summary>
        /// <param name="value"></param>
        /// <param name="targetType"></param>
        /// <param name="parameter"></param>
        /// <param name="culture"></param>
        /// <returns></returns>
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            var boolean = false;
            if (value is bool) boolean = (bool)value;
            return boolean ? Visibility.Visible : Visibility.Hidden;
        }

        /// <summary>
        ///  Method that converts vice versa 
        /// </summary>
        /// <param name="value"></param>
        /// <param name="targetType"></param>
        /// <param name="parameter"></param>
        /// <param name="culture"></param>
        /// <returns></returns>
        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return value is Visibility && (Visibility)value == Visibility.Visible;
        }
    }
}
