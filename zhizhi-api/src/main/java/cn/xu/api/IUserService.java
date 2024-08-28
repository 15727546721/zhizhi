package cn.xu.api;

public interface IUserService {

    String queryUserInfo(String req);

    String updateUserInfo(String req);

    String deleteUserInfo(String req);

    String creatUserInfo(String req);
}
