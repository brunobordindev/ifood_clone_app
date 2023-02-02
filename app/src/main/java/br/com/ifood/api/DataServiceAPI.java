package br.com.ifood.api;

import br.com.ifood.model.CEP;
import br.com.ifood.model.Usuario;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DataServiceAPI {

    @GET("{cep}/json/")
    Call<CEP> recuperarCEP(@Path("cep") String cep);
}
