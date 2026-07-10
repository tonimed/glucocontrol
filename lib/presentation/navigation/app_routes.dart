class AppRoutes {
  AppRoutes._();

  static const auth          = '/auth';
  static const home          = '/home';
  static const history       = '/history';
  static const chart         = '/chart/:period';
  static const addEditReading= '/reading/edit/:readingId';
  static const readingDetail = '/reading/:readingId';
  static const settings      = '/settings';

  // Helpers para navegar con parámetros
  static String chartPath(String period) => '/chart/$period';
  static String addReadingPath()          => '/reading/edit/-1';
  static String editReadingPath(int id)   => '/reading/edit/$id';
  static String readingDetailPath(int id) => '/reading/$id';
}
