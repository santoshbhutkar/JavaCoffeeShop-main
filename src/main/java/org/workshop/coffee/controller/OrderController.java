package org.workshop.coffee.controller;

import org.workshop.coffee.domain.Order;
import org.workshop.coffee.domain.OrderLine;
import org.workshop.coffee.domain.Product;
import org.workshop.coffee.export.ExportOrderConvertor;
import org.workshop.coffee.export.XML2OrderParser;
import org.workshop.coffee.export.XMLExporter;
import org.workshop.coffee.export.YamlImportExport;
import org.workshop.coffee.service.OrderService;
import org.workshop.coffee.service.PersonService;
import org.workshop.coffee.service.ProductService;
import org.workshop.coffee.service.QrCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.google.zxing.WriterException;

@Controller
@RequestMapping("/orders")
public class OrderController {
    @Autowired private ProductService productService;
    @Autowired private OrderService orderService;
    @Autowired private PersonService personService;
    @Autowired private QrCodeService qrCodeService;




    @ModelAttribute("products")
    public List<Product> populateProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/myorders")
    public String showMyOrders(Model model, Principal principal) {
        var user = principal.getName();
        var person = personService.findByUsername(user);
        model.addAttribute("orders", orderService.findByPerson(person));
        model.addAttribute("isMyOrders", true);
        return "order/mylist";
    }

    @GetMapping(value = "/myorders/export.yaml", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public byte[] exportYaml(Principal principal) {
        var user = principal.getName();
        var person = personService.findByUsername(user);
        var orders = orderService.findByPerson(person);
        var yamlOutput = YamlImportExport.exportOrders(orders);
        return yamlOutput.getBytes(StandardCharsets.UTF_8);
    }

    @GetMapping(value = "/myorders/export.xml", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public byte[] exportXml(Principal principal) throws XMLStreamException, TransformerException {
        var user = principal.getName();
        var person = personService.findByUsername(user);
        var orders = orderService.findByPerson(person);
        var xmlOutput = XMLExporter.exportOrders(orders);
        return xmlOutput.getBytes(StandardCharsets.UTF_8);
    }

    @GetMapping("/myorders/import")
    public String index(Model model) {
        return "order/import";
    }

    @PostMapping( "/myorders/import")
    public String singleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes, Principal principal) {
        var user = principal.getName();
        var person = personService.findByUsername(user);

        if (file.isEmpty()) {
            return "redirect:/orders/myorders/import";
        }

        try {
            if (file.getContentType().contains("yaml") || fileExtIs(file, ".yaml") || fileExtIs(file, ".yml")) {
                YamlImportExport.importOrders(file.getInputStream(), person)
                        .forEach(orderService::save);
            } else if (file.getContentType().equals("text/xml") || fileExtIs(file, ".xml")) {
                var parser = new XML2OrderParser();
                parser.parse(file.getInputStream())
                        .stream()
                        .map(eo -> ExportOrderConvertor.createOrders(eo,person))
                        .forEach(orderService::save);;
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "redirect:/orders/myorders";
    }

    private boolean fileExtIs(MultipartFile file, String ext) {
        return file.getOriginalFilename().endsWith(ext);
    }


    @GetMapping("/add")
    public String showOrderForm(Model model) {
        Order order = new Order();
        order.setOrderDate(new Date());
        order.addOrderLine(new OrderLine());
        model.addAttribute("order", order);
        return "order/add";
    }

    @PostMapping(value = "/add", params = "addOrderLine")
    public String addOrderLine(Order order, BindingResult result) {
        OrderLine orderLine = new OrderLine();
        order.addOrderLine(orderLine);
        return "order/add";
    }

    @PostMapping(value = "/add", params = "removeOrderLine")
    public String removeOrderLine(Order order, BindingResult result, HttpServletRequest request) {
        int orderLineId = Integer.parseInt(request.getParameter("removeOrderLine"));
        order.getOrderLines().remove(orderLineId);

        return "order/add";
    }

    @PostMapping("/add")
    public String saveOrder(@Valid Order order, BindingResult result, Principal principal, RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            try {
                List<OrderLine> orderLines = order.getOrderLines();
                int lastOrderLineIndex = orderLines.size() - 1;

                if (orderLines.get(lastOrderLineIndex).getProduct() == null) {
                    orderLines.remove(lastOrderLineIndex);
                }

                if (orderLines.isEmpty()) {
                    order.addOrderLine(new OrderLine());
                    throw new Exception("At least one product must be added to the Order");
                }

                for (OrderLine orderLine : orderLines) {
                    var prod = orderLine.getProduct();
                    orderLine.setProductName(prod.getProductName());
                    orderLine.setPrice(prod.getPrice());

                    if (orderLine.getQuantity() == 0) {
                        throw new Exception("Quantity of product " + orderLine.getProductName() + " must be positive");
                    }
                }

                order.setPerson(personService.findByUsername(principal.getName()));
                order.getOrderLines().forEach(orderLine -> orderLine.setOrder(order));

                orderService.save(order);

                redirectAttributes.addFlashAttribute("message", "Your order has been placed successfully.");

                return "redirect:/orders/add";
            } catch (Exception e) {
                result.reject(null, e.getMessage());
            }
        }

        return "order/add";
    }

    @GetMapping
    public String listOrders(Model model) {
        model.addAttribute("orders", orderService.findAll());
        return "order/list";
    }

    @GetMapping(value = "/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] generateOrderQrCode(@PathVariable Long id, Principal principal) 
            throws WriterException, IOException {
        Order order = orderService.findById(id);
        
        // Security check: users can only generate QR codes for their own orders
        // Admins can generate for any order
        String username = principal.getName();
        boolean isAdmin = personService.findByUsername(username).getRoles() == org.workshop.coffee.domain.Role.ROLE_ADMIN;
        boolean isOwner = order.getPerson().getUsername().equals(username);
        
        if (!isAdmin && !isOwner) {
            throw new RuntimeException("You don't have permission to view this order's QR code.");
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String orderDate = dateFormat.format(order.getOrderDate());
        String customerName = order.getPerson().getFirstName() + " " + order.getPerson().getLastName();
        
        return qrCodeService.generateOrderQrCode(
            order.getId(),
            customerName,
            orderDate,
            order.getTotalAmount()
        );
    }

    @GetMapping(value = "/myorders/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] generateMyOrderQrCode(@PathVariable Long id, Principal principal) 
            throws WriterException, IOException {
        Order order = orderService.findById(id);
        
        // Verify the order belongs to the current user
        String username = principal.getName();
        if (!order.getPerson().getUsername().equals(username)) {
            throw new RuntimeException("You don't have permission to view this order's QR code.");
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String orderDate = dateFormat.format(order.getOrderDate());
        String customerName = order.getPerson().getFirstName() + " " + order.getPerson().getLastName();
        
        return qrCodeService.generateOrderQrCode(
            order.getId(),
            customerName,
            orderDate,
            order.getTotalAmount()
        );
    }
}
