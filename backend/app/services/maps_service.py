import requests
from typing import Optional

from app.core.config import settings


class MapsService:
    """Google Maps API integration for geolocation and reverse geocoding."""
    
    @staticmethod
    def reverse_geocode(latitude: float, longitude: float) -> Optional[dict]:
        """
        Convert latitude/longitude to address using Google Reverse Geocoding API.
        Returns dict with address info or None if API call fails.
        """
        if not settings.google_maps_api_key:
            return None
        
        url = "https://maps.googleapis.com/maps/api/geocode/json"
        params = {
            "latlng": f"{latitude},{longitude}",
            "key": settings.google_maps_api_key,
        }
        
        try:
            response = requests.get(url, params=params, timeout=5)
            if response.status_code == 200:
                data = response.json()
                if data.get("results"):
                    result = data["results"][0]
                    return {
                        "address": result.get("formatted_address"),
                        "country": next(
                            (c["long_name"] for c in result["address_components"] 
                             if "country" in c["types"]), None
                        ),
                        "city": next(
                            (c["long_name"] for c in result["address_components"] 
                             if "locality" in c["types"]), None
                        ),
                    }
        except Exception as e:
            print(f"Reverse geocoding error: {e}")
        
        return None

    @staticmethod
    def geocode_address(address: str) -> Optional[dict]:
        """
        Convert address to latitude/longitude using Google Geocoding API.
        Returns dict with lat, lng or None if API call fails.
        """
        if not settings.google_maps_api_key:
            return None
        
        url = "https://maps.googleapis.com/maps/api/geocode/json"
        params = {
            "address": address,
            "key": settings.google_maps_api_key,
        }
        
        try:
            response = requests.get(url, params=params, timeout=5)
            if response.status_code == 200:
                data = response.json()
                if data.get("results"):
                    result = data["results"][0]
                    location = result["geometry"]["location"]
                    return {
                        "latitude": location["lat"],
                        "longitude": location["lng"],
                        "address": result.get("formatted_address"),
                    }
        except Exception as e:
            print(f"Geocoding error: {e}")
        
        return None
