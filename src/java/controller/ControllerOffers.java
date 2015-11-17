/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import dtos.OfertasDTO;
import dtos.PedidoSobreOfertaDTO;
import facade.FacadeOfertas;
import facade.FacadePedidoSobreOferta;
import facade.FacadeProductosAsociadosUsuarios;
import facade.FacadeUsuarios;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mail.Mail;

/**
 *
 * @author Mona
 */
public class ControllerOffers extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        FacadeOfertas facadeOffer = new FacadeOfertas();
        FacadeProductosAsociadosUsuarios facadeProAsoUser = new FacadeProductosAsociadosUsuarios();
        OfertasDTO ofDto = new OfertasDTO();
        String salida = "";
        try {
            if (request.getParameter("btnRegistrarMiOferta") != null && request.getParameter("registrarMiOferta") != null) {
                String correo = request.getParameter("txtCorreo").trim();
                out.print(correo);
                String nombreProducto = request.getParameter("txtNombreProducto").trim();
                int cantidad = Integer.parseInt(request.getParameter("txtCantidad").trim());
                float precio = Float.parseFloat(request.getParameter("txtPrecio").trim());
                ofDto.setProductosAsociadosUsuariosId(Integer.parseInt(request.getParameter("txtIdProAso")));
                ofDto.setCantidad(Integer.parseInt(request.getParameter("txtCantidad").trim()));
                ofDto.setPrecio(Float.parseFloat(request.getParameter("txtPrecio").trim()));
                ofDto.setCantidadFinal(Integer.parseInt(request.getParameter("txtCantidad").trim()));

                String salidaDos = facadeOffer.registrarOferta(ofDto);

                if (salidaDos.equals("ok")) {
                    FacadeProductosAsociadosUsuarios facadeProAso = new FacadeProductosAsociadosUsuarios();
                    String salidaTres = facadeProAso.cambiarEstadoProAso(Integer.parseInt(request.getParameter("txtIdProAso").trim()));

                    if (salidaTres.equals("ok")) {
                        response.sendRedirect("pages/misproductos.jsp?msgSalida= <strong>Su oferta ha sido registrada.</Strong>");
                        Mail.sendMail("Confirmación de oferta", "Su oferta ha sido registrada satisfactoriamente.<br>"
                                + " Acuerdate que tiene las siguientes caracteristicas: <br>"
                                + " Nombre del producto: " + nombreProducto + " <br>"
                                + " Cantidad: " + cantidad + " <br>"
                                + " Precio: $ " + precio + " <br>"
                                + " Recuerda que al postular una oferta, tienes 15 días despues de que la publico, <br> "
                                + " si tienes algún inconveniente o deseas actualizar la cantidad, puedes <br>"
                                + " hacerlo, ingresando nuevamente al sistema. ", correo);
                    } else {
                        response.sendRedirect("pages/misproductos.jsp?msgSalida= <strong>El estado no fue modificado.</Strong>");
                    }
                } else {
                    response.sendRedirect("pages/misproductos.jsp?msgSalida= <strong>La oferta no pudo ser registrada.</Strong>");
                }
            } else if (request.getParameter("idProAso") != null) {
                salida = facadeProAsoUser.cambiarEstadoProAsoDdeshabilitar(Integer.parseInt(request.getParameter("idProAso")));
                response.sendRedirect("pages/misproductos.jsp?msgSalida=<strong>El producto ha sido deshabilitado.</strong>");
            } else if (request.getParameter("btnModificarMiOferta") != null && request.getParameter("modificarMiOferta") != null) {

                ofDto.setIdOfertas(Integer.parseInt(request.getParameter("txtIdOferta").trim()));
                ofDto.setCantidad(Integer.parseInt(request.getParameter("txtCantidad").trim()));

                salida = facadeOffer.actualizarMiOferta(ofDto);
                response.sendRedirect("pages/misofertas.jsp?msgSalida= <strong>Su oferta ha sido actualizada.</Strong>");

            } else if (request.getParameter("idPedido") != null) {
                FacadePedidoSobreOferta facadePedido = new FacadePedidoSobreOferta();

                int cambioEstado = facadePedido.despacharOferta(Integer.parseInt(request.getParameter("idPedido")));
                if (cambioEstado > 0) {
                    FacadeUsuarios facadeUsuario = new FacadeUsuarios();
                    String correoDistribuidor = facadeUsuario.enviarCorreoAlDespacharUnaOferta(Integer.parseInt(request.getParameter("idPedido")));
                    String mensaje = "El pedido ha sido despachado a su cliente. Al cual se le enviara un correo "
                            + " para que se entere que su pedido ha sido despachado. ";
                    response.sendRedirect("pages/consultarmispedidossobremisofertas.jsp?tipo=1&msg=" + mensaje);
                    Mail.sendMail("Pedido sobre oferta ha sido despachado",
                            "<h3>Confirmación de envio de oferta</h3>"
                            + " La promoción que ha solicitado, ha sido despachado. <br><br>"
                            + " Gracias por pertenecer a SIGAA <br>"
                            + " Persona encargada: Stefhany Alfonso Rincón <br>"
                            + " Líneas de atención: 3213018539", correoDistribuidor);
                } else {
                    String msg = "No se pudo despachar el pedido.";
                    response.sendRedirect("pages/consultarmispedidossobremisofertas.jsp?tipo=0&msg=" + msg);
                }

            } else {
                out.print("Esta ingresando de forma infraudalenta.");
            }
        } finally {
            out.close();

        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
