    
 import 'package:get_it/get_it.dart'; // add [get_it:] to pubspec.yaml  
 import '../../core/network/network.dart';
 import 'package:shared_preferences/shared_preferences.dart'; // add [shared_preferences:] to pubspec.yaml

final sl = GetIt.instance;

Future<void> init() async {


    

//! Core
  sl.registerLazySingleton<NetworkInfo>(() => NetworkInfoImpl(sl()));

        
        
//! External        
   final sharedPreferences = await SharedPreferences.getInstance();
   sl.registerLazySingleton(() => sharedPreferences);     
       
// Bloc  
       
       
       
// Usecases

           
           
           
// Repository          
       



// Datasources


}