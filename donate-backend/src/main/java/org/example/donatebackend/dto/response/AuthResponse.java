package org.example.donatebackend.dto.response;

public class AuthResponse {
    private UserResponse userResponse;
    private StreamerDetailResponse streamerDetailReponse;
    private String token;
    private String refreshToken;

    public UserResponse getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(UserResponse userResponse) {
        this.userResponse = userResponse;
    }

    public StreamerDetailResponse getStreamerDetailReponse() {
        return streamerDetailReponse;
    }

    public void setStreamerDetailReponse(StreamerDetailResponse streamerDetailReponse) {
        this.streamerDetailReponse = streamerDetailReponse;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "userResponse=" + userResponse +
                ", streamerDetailReponse=" + streamerDetailReponse +
                ", token='" + token + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }
}
